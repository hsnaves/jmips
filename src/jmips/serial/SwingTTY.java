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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JComponent;
import javax.swing.Timer;

public class SwingTTY extends JComponent implements TTY {
	private static final long serialVersionUID = -8337907135507323599L;
	private final Font terminalFont;
	private final int numDisplayRows;
	private final int numDisplayColumns;
	private final int totalRows;
	private int cursorDisplayRow;
	private int cursorDisplayColumn;
	private int cursorRowDifference;
	private int displayRow, baseRow;
	
	private int currentForegroundColor;
	private int currentBackgroundColor;
	private int inputState;
	private final List<Integer> escapeParameters = new LinkedList<Integer>();
	private int currentEscapeParameter;

	private final Queue<Byte> inputChars = new ConcurrentLinkedQueue<Byte>();

	private final TerminalCharacter[][] characterMap;
	private final Timer cursorTimer;
	private final AtomicBoolean cursorBlink = new AtomicBoolean(true);
	private boolean echoEnabled;

	public SwingTTY() {
		this(25, 80, 1000);
	}

	public SwingTTY(int numRows, int numColumns, int totalRows) {
		this.terminalFont = new Font(Font.MONOSPACED, Font.PLAIN, 14);
		this.numDisplayRows = numRows;
		this.numDisplayColumns = numColumns;
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
				appendCharacter((byte) event.getKeyChar());
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
						scrollDisplay(-(SwingTTY.this.numDisplayRows - 1));
					} else if (event.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
						scrollDisplay(SwingTTY.this.numDisplayRows - 1);
					}
				} else {
					if (event.getKeyCode() == KeyEvent.VK_UP) {
						appendString("\u001B[A");
					} else if (event.getKeyCode() == KeyEvent.VK_DOWN) {
						appendString("\u001B[B");
					} else if (event.getKeyCode() == KeyEvent.VK_LEFT) {
						appendString("\u001B[D");
					} else if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
						appendString("\u001B[C");
					} else if (event.getKeyCode() == KeyEvent.VK_HOME) {
						appendString("\u001B[0H");
					} else if (event.getKeyCode() == KeyEvent.VK_END) {
						appendString("\u001B[0F");
					} else if (event.getKeyCode() == KeyEvent.VK_PAGE_UP) {
						appendString("\u001B[5~");
					} else if (event.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
						appendString("\u001B[6~");
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
	}

	public void setEchoEnabled(boolean echoEnabled) {
		this.echoEnabled = echoEnabled;
	}

	private void appendCharacter(byte b) {
		inputChars.add(b);
		if (echoEnabled) write(b);
	}

	private void appendString(String str) {
		for(int index = 0; index < str.length(); index++) {
			appendCharacter((byte) str.charAt(index));
		}
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
		for(int row = 0; row < numDisplayRows; row++)
			for(int column = 0; column < numDisplayColumns; column++)
				characterMap[row][column].change(' ', currentForegroundColor, currentBackgroundColor);
		this.cursorDisplayRow = this.cursorDisplayColumn = 0;
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
		final int screenWidth = numDisplayColumns * fontMetrics.charWidth(' ');
		final int screenHeight = numDisplayRows * fontMetrics.getHeight();
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
		graphics2D.fillRect(0, 0, numDisplayColumns * charWidth, numDisplayRows * charHeight);

		for (int rrow = 0; rrow < numDisplayRows; rrow++) {
			int row = rrow + this.displayRow;
			if (row >= totalRows) row -= totalRows;
			for (int col = 0; col < numDisplayColumns; col++) {
				TerminalCharacter character = characterMap[row][col];
				Color backgroundColor = character.getBackgroundAsAWT();
				Color foregroundColor = character.getForegroundAsAWT();
				if (rrow == this.cursorDisplayRow && col == this.cursorDisplayColumn && this.cursorBlink.get()) {
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
				this.cursorDisplayRow--;
				this.cursorRowDifference++;
				if (this.displayRow == this.totalRows)
					this.displayRow = 0;
			}
		} else {
			amount = -amount;
			int end = this.baseRow + this.numDisplayRows;
			if (end >= this.totalRows) end -= this.totalRows;
			for(int i = 0; i < amount; i++) {
				if (this.displayRow == end) break;
				this.displayRow--;
				if (this.displayRow < 0) this.displayRow = this.totalRows - 1;
				this.cursorDisplayRow++;
				this.cursorRowDifference--;
			}
		}
		refreshUI();
	}

	private void scroll() {
		this.cursorDisplayRow--;
		this.baseRow++;
		if (this.baseRow >= this.totalRows) {
			this.baseRow = 0;
		}
		this.displayRow = this.baseRow;
		int row = this.baseRow + this.numDisplayRows - 1;
		if (row >= this.totalRows) row -= this.totalRows;
		for(int i = 0; i < this.numDisplayColumns; i++) {
			characterMap[row][i].change(' ', COLOR_WHITE, COLOR_BLACK);
		}
	}

	private void clear(int row, int column) {
		row += this.baseRow;
		if (row >= this.totalRows) row -= this.totalRows;
		for(int i = column; i < this.numDisplayColumns; i++)
			characterMap[row][i].change(' ', COLOR_WHITE, COLOR_BLACK);
	}

	@Override
	public void write(byte b) {
		char c = (char) b;
		if (this.displayRow != this.baseRow) {
			this.cursorDisplayRow += this.cursorRowDifference;
			this.displayRow = this.baseRow;
		}
		this.cursorRowDifference = 0;

		if (this.inputState == 0) {
			switch(c) {
			case '\n':
				this.cursorDisplayRow++;
				this.cursorDisplayColumn = 0;
				break;
			case '\r':
				this.cursorDisplayColumn = 0;
				break;
			case '\u0008': // backspace
				if (this.cursorDisplayColumn > 0) {
					this.cursorDisplayColumn--;
				}
				break;
			case '\t':
				int newcolumn = (this.cursorDisplayColumn + 8) & ~7;
				if (newcolumn < numDisplayColumns) {
					this.cursorDisplayColumn = newcolumn;
				} else {
					this.cursorDisplayColumn = 0;
					this.cursorDisplayRow++;
				}
				break;
			case '\u001B': // ESC
				this.inputState = 1;
				break;
			default:
				int row = this.cursorDisplayRow + this.baseRow;
				if (row >= totalRows) row -= totalRows;
				characterMap[row][this.cursorDisplayColumn].change(c, currentForegroundColor, currentBackgroundColor);

				this.cursorDisplayColumn++;
				if (this.cursorDisplayColumn == numDisplayColumns) {
					this.cursorDisplayRow++;
					this.cursorDisplayColumn = 0;
				}
				break;
			}
			if (this.cursorDisplayRow >= numDisplayRows) {
				scroll();
			}
			showCursor();
		} else if (this.inputState == 1) {
			if (c == '[') {
				this.escapeParameters.clear();
				this.currentEscapeParameter = 0;
				this.inputState = 2;
			} else {
				this.inputState = 0;
			}
		} else {
			if (c >= '0' && c <= '9') {
				this.currentEscapeParameter = this.currentEscapeParameter * 10 + (c - '0');
			} else {
				this.escapeParameters.add(this.currentEscapeParameter);
				this.currentEscapeParameter = 0;
				if (c != ';') {
					int n;
					switch(c) {
					case 'A': // up
						n = this.escapeParameters.get(0);
						if (n < 1) n = 1;
						this.cursorDisplayRow -= n;
						if (this.cursorDisplayRow < 0)
							this.cursorDisplayRow = 0;
						break;
					case 'B': // down
						n = this.escapeParameters.get(0);
						if (n < 1) n = 1;
						this.cursorDisplayRow += n;
						if (this.cursorDisplayRow >= this.numDisplayRows)
							this.cursorDisplayRow = this.numDisplayRows - 1;
						break;
					case 'C': // right
						n = this.escapeParameters.get(0);
						if (n < 1) n = 1;
						this.cursorDisplayColumn += n;
						if (this.cursorDisplayColumn >= this.numDisplayColumns)
							this.cursorDisplayColumn = this.numDisplayColumns - 1;
						break;
					case 'D': // left
						n = this.escapeParameters.get(0);
						if (n < 1) n = 1;
						this.cursorDisplayColumn -= n;
						if (this.cursorDisplayColumn < 0)
							this.cursorDisplayColumn = 0;
						break;
					case 'H': // home
						int newRow = this.escapeParameters.get(0) - 1;
						int newColumn = 0;
						if (this.escapeParameters.size() > 1) {
							newColumn = this.escapeParameters.get(1) - 1;
						}
						if (newRow < 0) newRow = 0;
						else if (newRow >= numDisplayRows) newRow = numDisplayRows - 1;
						if (newColumn < 0) newColumn = 0;
						else if (newColumn >= numDisplayColumns) newColumn = numDisplayColumns - 1;
						this.cursorDisplayRow = newRow;
						this.cursorDisplayColumn = newColumn;
						break;
					case 'J':
						clear(this.cursorDisplayRow, this.cursorDisplayColumn);
						for(int j = this.cursorDisplayRow + 1; j < this.numDisplayRows; j++)
							clear(j, 0);
						break;
					case 'K':
						clear(this.cursorDisplayRow, this.cursorDisplayColumn);
						break;
					case 'm':
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
					default:
						System.out.println(c);
					}
					this.inputState = 0;
					showCursor();
				}
			}
		}
	}

	@Override
	public boolean available() {
		return !inputChars.isEmpty();
	}

	@Override
	public byte read() {
		return inputChars.remove();
	}

	@Override
	public void reset() {
		open();
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
