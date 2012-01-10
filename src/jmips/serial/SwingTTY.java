package jmips.serial;

import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JComponent;
import javax.swing.Timer;

public class SwingTTY extends JComponent implements TTY {
	private static final long serialVersionUID = -8337907135507323599L;
	private final Font terminalFont;
	private final int numRows;
	private final int totalRows;
	private final int numColumns;
	private int cursorRow;
	private int cursorColumn;
	private int cursorRowDifference;
	private int displayRow, baseRow;
	
	private int currentForegroundColor;
	private int currentBackgroundColor;
	private int inputState;
	private final List<Integer> escapeParameters = new LinkedList<Integer>();
	private int currentEscapeParameter;

	private final List<Byte> inputBytes = new ArrayList<Byte>();

	private final TerminalCharacter[][] characterMap;
	private final Timer cursorTimer;
	private final AtomicBoolean cursorBlink = new AtomicBoolean(true);
	private boolean echoEnabled;

	public SwingTTY() {
		this(40, 120, 1000);
	}

	public SwingTTY(int numRows, int numColumns, int totalRows) {
		this.terminalFont = new Font(Font.MONOSPACED, Font.PLAIN, 14);
		this.numRows = numRows;
		this.numColumns = numColumns;
		this.totalRows = totalRows;

		this.cursorTimer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (!cursorBlink.compareAndSet(true, false))
					cursorBlink.set(true);
				refreshUI();
			}
		});
		this.cursorTimer.setRepeats(true);
		this.characterMap = new TerminalCharacter[totalRows][numColumns];
		for(int row = 0; row < totalRows; row++)
			for(int column = 0; column < numColumns; column++)
				characterMap[row][column] = new TerminalCharacter(' ', COLOR_WHITE, COLOR_BLACK);

		this.clearScreen();
		this.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent event) {
				appendByte((byte) event.getKeyChar());
			}

			@Override
			public void keyReleased(KeyEvent event) {
			}

			@Override
			public void keyPressed(KeyEvent event) {
				if (event.isControlDown()) {
					if (event.getKeyCode() == KeyEvent.VK_UP) {
						scrollDisplay(-1);
					} else if (event.getKeyCode() == KeyEvent.VK_DOWN) {
						scrollDisplay(+1);
					} else if (event.getKeyCode() == KeyEvent.VK_PAGE_UP) {
						scrollDisplay(-(SwingTTY.this.numRows - 1));
					} else if (event.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
						scrollDisplay(SwingTTY.this.numRows - 1);
					}
				} else {
					if (event.getKeyCode() == KeyEvent.VK_UP) {
						appendByte((byte) 27);
						appendByte((byte) 91);
						appendByte((byte) 65);
					} else if (event.getKeyCode() == KeyEvent.VK_DOWN) {
						appendByte((byte) 27);
						appendByte((byte) 91);
						appendByte((byte) 66);
					} else if (event.getKeyCode() == KeyEvent.VK_LEFT) {
						appendByte((byte) 27);
						appendByte((byte) 91);
						appendByte((byte) 68);
					} else if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
						appendByte((byte) 27);
						appendByte((byte) 91);
						appendByte((byte) 67);
					} else if (event.getKeyCode() == KeyEvent.VK_HOME) {
						appendByte((byte) 27);
						appendByte((byte) 91);
						appendByte((byte) '0');
						appendByte((byte) 'H');
					} else if (event.getKeyCode() == KeyEvent.VK_END) {
						appendByte((byte) 27);
						appendByte((byte) 91);
						appendByte((byte) '0');
						appendByte((byte) 'F');
					} else if (event.getKeyCode() == KeyEvent.VK_PAGE_UP) {
						appendByte((byte) 27);
						appendByte((byte) 91);
						appendByte((byte) '5');
						appendByte((byte) '~');
					} else if (event.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
						appendByte((byte) 27);
						appendByte((byte) 91);
						appendByte((byte) '6');
						appendByte((byte) '~');
					}
				}
			}
		});
		this.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent event) {
			}
			
			@Override
			public void mousePressed(MouseEvent event) {
			}
			
			@Override
			public void mouseExited(MouseEvent event) {
			}
			
			@Override
			public void mouseEntered(MouseEvent event) {
			}
			
			@Override
			public void mouseClicked(MouseEvent event) {
				requestFocusInWindow();
			}
		});
		setFocusable(true);
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.<AWTKeyStroke> emptySet());
		setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.<AWTKeyStroke> emptySet());
		//setFocusCycleRoot(true);
	}

	public void setEchoEnabled(boolean echoEnabled) {
		this.echoEnabled = echoEnabled;
	}

	private void appendByte(byte b) {
		inputBytes.add(b);
		if (echoEnabled) write(b);
	}

	public void open() {
		cursorTimer.start();
		clearScreen();
	}

	public void close() {
		cursorTimer.stop();
	}

	public void clearScreen() {
		this.displayRow = 0;
		this.baseRow = 0;
		this.currentForegroundColor = COLOR_WHITE;
		this.currentBackgroundColor = COLOR_BLACK;
		for(int row = 0; row < numRows; row++)
			for(int column = 0; column < numColumns; column++)
				characterMap[row][column].change(' ', currentForegroundColor, currentBackgroundColor);
		this.cursorRow = this.cursorColumn = 0;
		this.inputState = 0;
	}

	private void showCursor() {
		if (!cursorBlink.get()) {
			cursorBlink.set(true);
		}
		refreshUI();
	}

	private void refreshUI() {
		repaint();
	}

	@Override
	public Dimension getPreferredSize() {
		FontMetrics fontMetrics = getGraphics().getFontMetrics(terminalFont);
		final int screenWidth = numColumns * fontMetrics.charWidth(' ');
		final int screenHeight = numRows * fontMetrics.getHeight();
		return new Dimension(screenWidth, screenHeight);
	}

	@Override
	protected void paintComponent(Graphics g) {
		final Graphics2D graphics2D = (Graphics2D) g.create();
		graphics2D.setFont(terminalFont);
		graphics2D.setColor(Color.BLACK);
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		                            RenderingHints.VALUE_ANTIALIAS_ON);

		final FontMetrics fontMetrics = getGraphics().getFontMetrics(terminalFont);
		final int charWidth = fontMetrics.charWidth(' ');
		final int charHeight = fontMetrics.getHeight();
		graphics2D.fillRect(0, 0, numColumns * charWidth, numRows * charHeight);

		for (int rrow = 0; rrow < numRows; rrow++) {
			int row = rrow + this.displayRow;
			if (row >= totalRows) row -= totalRows;
			for (int col = 0; col < numColumns; col++) {
				TerminalCharacter character = characterMap[row][col];
				Color backgroundColor = character.getBackgroundAsAWT();
				Color foregroundColor = character.getForegroundAsAWT();
				if (rrow == this.cursorRow && col == this.cursorColumn && this.cursorBlink.get()) {
					foregroundColor = character.getBackgroundAsAWT();
					backgroundColor = character.getForegroundAsAWT();
				}
				graphics2D.setColor(backgroundColor);
				graphics2D.fillRect(col * charWidth, rrow * charHeight, charWidth, charHeight);
				graphics2D.setColor(foregroundColor);
				graphics2D.drawString(character.toString(), col * charWidth,
				                      ((rrow + 1) * charHeight) - fontMetrics.getDescent());
			}
		}
		graphics2D.dispose();
	}

	private void scrollDisplay(int amount) {
		if (amount > 0) {
			for(int i = 0; i < amount; i++) {
				if (this.displayRow == this.baseRow) break;
				this.displayRow++;
				this.cursorRow--;
				this.cursorRowDifference++;
				if (this.displayRow == this.totalRows)
					this.displayRow = 0;
			}
		} else {
			amount = -amount;
			int end = this.baseRow + this.numRows;
			if (end >= this.totalRows) end -= this.totalRows;
			for(int i = 0; i < amount; i++) {
				if (this.displayRow == end) break;
				this.displayRow--;
				if (this.displayRow < 0) this.displayRow = this.totalRows - 1;
				this.cursorRow++;
				this.cursorRowDifference--;
			}
		}
		refreshUI();
	}

	private void scroll() {
		this.cursorRow--;
		this.baseRow++;
		if (this.baseRow >= this.totalRows) {
			this.baseRow = 0;
		}
		this.displayRow = this.baseRow;
		int row = this.baseRow + this.numRows - 1;
		if (row >= this.totalRows) row -= this.totalRows;
		for(int i = 0; i < this.numColumns; i++) {
			characterMap[row][i].change(' ', COLOR_WHITE, COLOR_BLACK);
		}
	}

	@Override
	public void write(byte b) {
		if (this.displayRow != this.baseRow) {
			this.cursorRow += this.cursorRowDifference;
			this.displayRow = this.baseRow;
		}
		this.cursorRowDifference = 0;

		if (this.inputState == 0) {
			switch(b) {
			case 10:
				this.cursorRow++;
				this.cursorColumn = 0;
				break;
			case 13:
				this.cursorColumn = 0;
				break;
			case 8:
				if (this.cursorColumn > 0) {
					this.cursorColumn--;
				}
				break;
			case 9:
				int newcolumn = (this.cursorColumn + 8) & ~7;
				if (newcolumn < numColumns) {
					this.cursorColumn = newcolumn;
				} else {
					this.cursorColumn = 0;
					this.cursorRow++;
				}
				break;
			case 27:
				this.inputState = 1;
				break;
			default:
				int row = this.cursorRow + this.baseRow;
				if (row >= totalRows) row -= totalRows;
				characterMap[row][this.cursorColumn].change((char) b, currentForegroundColor, currentBackgroundColor);

				this.cursorColumn++;
				if (this.cursorColumn == numColumns) {
					this.cursorRow++;
					this.cursorColumn = 0;
				}
				break;
			}
			if (this.cursorRow >= numRows) {
				scroll();
			}
			showCursor();
		} else if (this.inputState == 1) {
			if (b == 91) {
				this.escapeParameters.clear();
				this.currentEscapeParameter = 0;
				this.inputState = 2;
			} else {
				this.inputState = 0;
			}
		} else {
			if (b >= 48 && b < 58) {
				this.currentEscapeParameter = this.currentEscapeParameter * 10 + (b - 48);
			} else {
				this.escapeParameters.add(this.currentEscapeParameter);
				this.currentEscapeParameter = 0;
				if (b != 59) {
					int n;
					switch(b) {
					case 65:
						n = this.escapeParameters.get(0);
						if (n < 1) n = 1;
						this.cursorRow -= n;
						if (this.cursorRow < 0)
							this.cursorRow = 0;
						break;
					case 66:
						n = this.escapeParameters.get(0);
						if (n < 1) n = 1;
						this.cursorRow += n;
						if (this.cursorRow >= this.numRows)
							this.cursorRow = this.numRows - 1;
						break;
					case 67:
						n = this.escapeParameters.get(0);
						if (n < 1) n = 1;
						this.cursorColumn += n;
						if (this.cursorColumn >= this.numColumns)
							this.cursorColumn = this.numColumns - 1;
						break;
					case 68:
						n = this.escapeParameters.get(0);
						if (n < 1) n = 1;
						this.cursorColumn -= n;
						if (this.cursorColumn < 0)
							this.cursorColumn = 0;
						break;
					case 72:
						int newRow = this.escapeParameters.get(0) - 1;
						int newColumn = 0;
						if (this.escapeParameters.size() > 1) {
							newColumn = this.escapeParameters.get(1) - 1;
						}
						if (newRow < 0) newRow = 0;
						else if (newRow >= numRows) newRow = numRows - 1;
						if (newColumn < 0) newColumn = 0;
						else if (newColumn >= numColumns) newColumn = numColumns - 1;
						this.cursorRow = newRow;
						this.cursorColumn = newColumn;
						break;
					case 74:
						break;
					case 75:
						break;
					case 109:
						for(int i = 0; i < this.escapeParameters.size(); i++) {
							int p = this.escapeParameters.get(i);
							if (p >= 30 && p <= 37) {
								this.currentForegroundColor = p - 30;
							} else if (p >= 40 && p <= 47) {
								this.currentBackgroundColor = p - 40;
							} else {
								this.currentForegroundColor = COLOR_WHITE;
								this.currentBackgroundColor = COLOR_BLACK;
							}
						}
						break;
					case 110:
						break;
					}
					this.inputState = 0;
					showCursor();
				}
			}
		}
	}

	@Override
	public boolean available() {
		return !inputBytes.isEmpty();
	}

	@Override
	public byte read() {
		return inputBytes.remove(0);
	}


	public static final int COLOR_BLACK = 0;
	public static final int COLOR_RED = 1;
	public static final int COLOR_GREEN = 2;
	public static final int COLOR_YELLOW = 3;
	public static final int COLOR_BLUE = 4;
	public static final int COLOR_MAGENTA = 5;
	public static final int COLOR_CYAN = 6;
	public static final int COLOR_WHITE = 7;
	
	private static class TerminalCharacter {
		private char character;
		private int foreground;
		private int background;

		private static final Color[] colorMap = {
			new Color(0x00, 0x00, 0x00),
			new Color(0xFF, 0x00, 0x00),
			new Color(0x00, 0xFF, 0x00),
			new Color(0xFF, 0xFF, 0x00),
			new Color(0x00, 0x00, 0xFF),
			new Color(0xFF, 0x00, 0xFF),
			new Color(0x00, 0xFF, 0xFF),
			new Color(0xFF, 0xFF, 0xFF)
		};

		public TerminalCharacter(char character, int foreground, int background) {
			change(character, foreground, background);
		}

		public void change(char character, int foreground, int background) {
			this.character = character;
			this.foreground = foreground;
			this.background = background;
		}

		public Color getForegroundAsAWT() {
			return colorMap[foreground];
		}

		public Color getBackgroundAsAWT() {
			return colorMap[background];
		}

		@Override
		public String toString() {
			return Character.toString(character);
		}
	}
}
