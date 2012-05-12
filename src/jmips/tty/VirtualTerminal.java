package jmips.tty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VirtualTerminal implements Serializable {
	private static final long serialVersionUID = 7506926367252041158L;

	public static final int VTMODE_WRAPOFF = 0x04;

	private int cursorX, cursorY;
	private int savedCursorX, savedCursorY;
	private int attributes, savedAttributes;

	private LineList screen;
	private LineList defaultScreen, alternateScreen;
	private LineList history;

	private int mode;
	private int state;

	private List<Object> args;

	private VirtualTerminalHandler handler;

	public VirtualTerminal() {
		this.defaultScreen = new LineList(80,25);
		this.alternateScreen = new LineList(80, 25);
		this.screen = this.defaultScreen;

		this.history = new LineList(80, 1000);
		this.args = new ArrayList<Object>();
	}

	public void setHander(VirtualTerminalHandler handler) {
		this.handler = handler;
	}

	public VirtualTerminalHandler getHandler() {
		return handler;
	}

	public void resize(int width, int height) {
		defaultScreen.resize(width, height);
		alternateScreen.resize(width, height);
		history.resize(width, history.getHeight());

		if (cursorX >= width) cursorX = width - 1;
		if (cursorY >= height) cursorY = height - 1;
	}

	public void setScreen(boolean alternate) {
		int scrollTop = screen.getScrollTop();
		int scrollBottom = screen.getScrollBottom();
		if (alternate) {
			screen = alternateScreen;
		} else {
			screen = defaultScreen;
		}
		screen.setScrollRegion(scrollTop, scrollBottom);
	}

	public void scroll(int amount) {
		if (screen.getScrollTop() == 0 && screen == defaultScreen && amount > 0) {
			while(amount > 0) {
				int[] line, copy;
				history.scroll(1, false, 0);
				copy = history.getLine(0);

				line = screen.getLine(0);
				for(int j = 0; j < line.length; j++)
					copy[j] = line[j];

				screen.scroll(1, true, attributes);
				amount--;
			}
		} else {
			screen.scroll(amount, true, attributes);
		}
	}

	public void bell() {
		if (handler != null) {
			handler.bell(this);
		}
	}

	public void cariageReturn() {
		cursorX = 0;
	}

	public void lineFeed() {
		if (cursorY != screen.getScrollBottom()) {
			cursorY++;
		} else {
			scroll(1);
		}
	}

	public void newLine() {
		cariageReturn();
		lineFeed();
	}

	public void tab() {
		cursorX = (cursorX + 8) & (~7);
		if (cursorX >= screen.getWidth()) {
			if ((mode & VTMODE_WRAPOFF) != 0) {
				cursorX = screen.getWidth() - 1;
			} else {
				cursorX = 0;
				lineFeed();
			}
		}
	}

	public void backTab() {
		if (cursorX > 0) {
			cursorX = (cursorX - 1) & (~7);
		}
	}

	public void backSpace() {
		if (cursorX > 0) {
			cursorX--;
		}
	}

	public void write(byte b) {
		
	}

	public int dataAt(int column, int row) {
		int[] line = screen.getLine(row);
		return line[column];
	}
}

class LineList implements Serializable {

	private static final long serialVersionUID = -2958876501570521087L;
	private ArrayList<int[]> lines;
	private int width, height;
	private int scrollTop, scrollBottom, scroll;

	public LineList(int width, int height) {
		this.width = width;
		this.height = height;
		scrollTop = scroll = 0;
		scrollBottom = height;
		lines = new ArrayList<int[]>(height);
		for(int i = 0; i < height; i++)
			lines.add(new int[width]);
	}

	public void resize(int width, int height) {
		if (height != this.height) {
			ArrayList<int[]> newLines = new ArrayList<int[]>(height);
			for(int i = 0; i < height; i++) {
				int[] line = getLine(i);
				newLines.add(line);
			}
			this.lines = newLines;
			this.scroll = Math.min(this.scroll, height - 1);
			this.scrollTop = Math.min(this.scrollTop, height - 1);
			if (this.scrollBottom == this.height - 1) {
				this.scrollBottom = height - 1;
			} else {
				this.scrollBottom = Math.min(this.scrollBottom, height - 1);
			}
			this.height = height;
		}

		if (this.width != width) {
			for(int i = 0; i < lines.size(); i++) {
				int[] line = lines.get(i);
				int[] newLine = new int[width];
				for(int j = 0; j < Math.min(line.length, width); j++)
					newLine[j] = line[j];
			}
			this.width = width;
		}
	}

	public int[] getLine(int pos) {
		pos = pos % height;
		if (pos < 0) pos += height;
		if (pos < scrollTop || pos >= scrollBottom)
			return lines.get(pos);
		pos += scroll - 2 * scrollTop;
		pos %= (scrollTop - scrollBottom + 1);
		pos += scrollTop;
		return lines.get(pos);
	}

	public void scroll(int amount, boolean reset, int attributes) {
		while (amount > 0) {
			if (reset) Arrays.fill(lines.get(scroll), attributes);
			scroll++;
			if (scroll > scrollBottom)
				scroll = scrollTop;
			amount--;
		}
		while (amount < 0) {
			scroll--;
			if (scroll < scrollTop)
				scroll = scrollBottom;
			if (reset) Arrays.fill(lines.get(scroll), attributes);
			amount++;
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getScrollTop() {
		return scrollTop;
	}

	public int getScrollBottom() {
		return scrollBottom;
	}

	public void resetScrolling() {
		while (scroll > scrollTop) {
			int[] firstLine = lines.get(scrollTop);
			for(int i = scrollTop + 1; i <= scrollBottom; i++) {
				lines.set(i - 1, lines.get(i));
			}
			lines.set(scrollBottom, firstLine);
			scroll--;
		}
	}

	public void setScrollRegion(int scrollTop, int scrollBottom) {
		if (scrollTop < 0) scrollTop = 0;
		if (scrollTop >= height) scrollTop = height - 1;
		if (scrollBottom < scrollTop) scrollBottom = scrollTop;
		if (scrollBottom >= height) scrollBottom = height - 1;

		resetScrolling();
		this.scroll = scrollTop;
		this.scrollTop = scrollTop;
		this.scrollBottom = scrollBottom;
	}

	public void insertChars(int column, int row, int count, int attributes) {
		count = Math.min(count, width - column);
		int[] line = getLine(row);
		for(int i = width - 1; i >= column + count; i--) {
			line[i] = line[i - count];
		}
		for(int i = column; i < column + count; i++) {
			line[i] = attributes;
		}
	}

	public void deleteChars(int column, int row, int count, int attributes) {
		count = Math.min(count, width - column);
		int[] line = getLine(row);
		for(int i = column; i < width - count; i++) {
			line[i] = line[i + count];
		}
		for(int i = width - count; i < width; i++) {
			line[i] = attributes;
		}
	}

	public void eraseChars(int column, int row, int count, int attributes) {
		int[] line = getLine(row);
		for(int i = column; i < column + count && i < width; i++) {
			line[i] = attributes;
		}
	}

	public void insertLines(int row, int count, int attributes) {
		// We assume that row is between scrollTop and scrollBottom
		row = Math.min(row, scrollBottom);
		row = Math.max(row, scrollTop);
		resetScrolling();
		count = Math.min(count, scrollBottom - row + 1);
		while(count > 0) {
			int[] last = lines.get(scrollBottom);
			for(int i = scrollBottom; i > row; i--) {
				lines.set(i, lines.get(i - 1));
			}
			Arrays.fill(last, attributes);
			lines.set(row, last);
			count--;
		}
	}

	public void deleteLines(int row, int count, int attributes) {
		// We assume that row is between scrollTop and scrollBottom
		row = Math.min(row, scrollBottom);
		row = Math.max(row, scrollTop);
		resetScrolling();
		count = Math.min(count, scrollBottom - row + 1);
		while(count > 0) {
			int[] first = lines.get(row);
			for(int i = row; i < scrollBottom; i++) {
				lines.set(i, lines.get(i + 1));
			}
			Arrays.fill(first, attributes);
			lines.set(scrollBottom, first);
			count--;
		}
	}

	public void clearLines(int row, int count, int attributes) {
		count = Math.min(count, height - row);
		for(int i = row; i < row + count; i++) {
			int[] line = getLine(i);
			Arrays.fill(line, attributes);
		}
	}

	public void clearLinePortion(int row, int startColumn, int endColumn, int attributes) {
		int[] line = getLine(row);
		startColumn = Math.max(0, startColumn);
		startColumn = Math.min(width - 1, startColumn);
		endColumn = Math.max(startColumn + 1, endColumn);
		endColumn = Math.min(width, endColumn);
		Arrays.fill(line, startColumn, endColumn, attributes);
	}

	public void insertColumns(int column, int count, int attributes) {
		for(int i = 0; i < height; i++) {
			insertChars(column, i, count, attributes);
		}
	}

	public void deleteColumns(int column, int count, int attributes) {
		for(int i = 0; i < height; i++) {
			deleteChars(column, i, count, attributes);
		}
	}

	public void reset(int attributes) {
		resetScrolling();
		clearLines(0, height, attributes);
	}
}

