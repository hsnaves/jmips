package jmips.tty;

import java.io.Serializable;

public class VirtualTerminalParser implements Serializable {
	public static final int ST_GROUND = 1; // Ground
	public static final int ST_ESC    = 2; // Escape
	public static final int ST_ESCINT = 3; // Escape Intermediate
	public static final int ST_CSI    = 4; // Control Sequence Introducer
	public static final int ST_CSIPAR = 5; // Control Sequence Parameter
	public static final int ST_CSIIGN = 6; // Control Sequence Ignore
	public static final int ST_CSIINT = 7; // Control Sequence Intermediate
	public static final int ST_DCS    = 8; // Device Control String
	public static final int ST_DCSPAR = 9; // Device Control Parameter
	public static final int ST_DCSIGN = 10; // Device Control Ignore
	public static final int ST_DCSINT = 11; // Device Control Intermediate
	public static final int ST_DCSPAS = 12; // Device Control Pass Through
	public static final int ST_OSC    = 13; // Operating System Command
	public static final int ST_APC    = 14; // Application Program Command

	public static final byte C0_NUL = 0x00; // Null
	public static final byte C0_SOH = 0x01; // Start of Heading
	public static final byte C0_STX = 0x02; // Start of text
	public static final byte C0_ETX = 0x03; // End of text
	public static final byte C0_EOT = 0x04; // End of transmission
	public static final byte C0_ENQ = 0x05; // Enquiry
	public static final byte C0_ACK = 0x06; // Acknowledge
	public static final byte C0_BEL = 0x07; // Bell
	public static final byte C0_BS  = 0x08; // Backspace
	public static final byte C0_HT  = 0x09; // Horizontal Tabulation
	public static final byte C0_LF  = 0x0A; // Line Feed
	public static final byte C0_VT  = 0x0B; // Vertical Tabulation
	public static final byte C0_FF  = 0x0C; // Form Feed
	public static final byte C0_CR  = 0x0D; // Carriage Return
	public static final byte C0_SO  = 0x0E; // Shift out
	public static final byte C0_SI  = 0x0F; // Shift in
	public static final byte C0_DLE = 0x10; // Data link escape
	public static final byte C0_DC1 = 0x11; // Device control 1
	public static final byte C0_DC2 = 0x12; // Device control 2
	public static final byte C0_DC3 = 0x13; // Device control 3
	public static final byte C0_DC4 = 0x14; // Device control 4
	public static final byte C0_NAK = 0x15; // Negative Acknowledge
	public static final byte C0_SYN = 0x16; // Synchronous idle
	public static final byte C0_ETB = 0x17; // End of transmission block
	public static final byte C0_CAN = 0x18; // Cancel
	public static final byte C0_EM  = 0x19; // End of Medium
	public static final byte C0_SUB = 0x1A; // Substitute
	public static final byte C0_ESC = 0x1B; // Escape
	public static final byte C0_FS  = 0x1C; // File separator
	public static final byte C0_GS  = 0x1D; // Group separator
	public static final byte C0_RS  = 0x1E; // Record separator
	public static final byte C0_US  = 0x1F; // Unit separator

	public static final byte C1_PAD = (byte) 0x80; // Padding character
	public static final byte C1_HOP = (byte) 0x81; // High octet preset
	public static final byte C1_BPH = (byte) 0x82; // Break permitted here
	public static final byte C1_NBH = (byte) 0x83; // No break here
	public static final byte C1_IND = (byte) 0x84; // Index
	public static final byte C1_NEL = (byte) 0x85; // Next line
	public static final byte C1_SSA = (byte) 0x86; // Start of selected area
	public static final byte C1_ESA = (byte) 0x87; // End of selected area
	public static final byte C1_HTS = (byte) 0x88; // Horizontal tabulation set
	public static final byte C1_HTJ = (byte) 0x89; // Horizontal tabulation with justification
	public static final byte C1_VTS = (byte) 0x8A; // Vertical tabulation set
	public static final byte C1_PLD = (byte) 0x8B; // Partial line down
	public static final byte C1_PLU = (byte) 0x8C; // Partial line up
	public static final byte C1_RI  = (byte) 0x8D; // Reverse line feed
	public static final byte C1_SS2 = (byte) 0x8E; // Single shift 2
	public static final byte C1_SS3 = (byte) 0x8F; // Single shift 3
	public static final byte C1_DCS = (byte) 0x90; // Device control string
	public static final byte C1_PU1 = (byte) 0x91; // Private Use 1
	public static final byte C1_PU2 = (byte) 0x92; // Private Use 2
	public static final byte C1_STS = (byte) 0x93; // Set Transmit state
	public static final byte C1_CCH = (byte) 0x94; // Cancel character
	public static final byte C1_MW  = (byte) 0x95; // Message waiting
	public static final byte C1_SPA = (byte) 0x96; // Start of protected area
	public static final byte C1_EPA = (byte) 0x97; // End of protected area
	public static final byte C1_SOS = (byte) 0x98; // Start of string
	public static final byte C1_SGCI = (byte) 0x99; // Single graphic character introducer
	public static final byte C1_SCI = (byte) 0x9A; // Single character introducer
	public static final byte C1_CSI = (byte) 0x9B; // Control sequence introducer
	public static final byte C1_ST  = (byte) 0x9C; // String terminator
	public static final byte C1_OSC = (byte) 0x9D; // Operating System command
	public static final byte C1_PM  = (byte) 0x9E; // Privacy message
	public static final byte C1_APC = (byte) 0x9F; // Application Program command

	private static void writeGround(VirtualTerminal vt, byte b) {
		switch(b) {
		}
	}

	private static void writeEscape(VirtualTerminal vt, byte b) {
	}

	private static void writeEscapeIntermediate(VirtualTerminal vt, byte b) {
	}

	private static void writeControlSequenceIntroducer(VirtualTerminal vt, byte b) {
	}

	private static void writeControlSequenceParam(VirtualTerminal vt, byte b) {
	}

	private static void writeControlSequenceIgnore(VirtualTerminal vt, byte b) {
	}

	private static void writeControlSequenceIntermediate(VirtualTerminal vt, byte b) {
	}

	private static void writeDeviceControlString(VirtualTerminal vt, byte b) {
	}

	private static void writeDeviceControlParam(VirtualTerminal vt, byte b) {
	}

	private static void writeDeviceControlIgnore(VirtualTerminal vt, byte b) {
	}

	private static void writeDeviceControlIntermediate(VirtualTerminal vt, byte b) {
	}

	private static void writeDeviceControlStringPassThrough(VirtualTerminal vt, byte b) {
	}

	private static void writeOperatingSystemCommand(VirtualTerminal vt, byte b) {
	}

	private static void writeApplicationProgramCommand(VirtualTerminal vt, byte b) {
	}


	public static void write(VirtualTerminal vt, byte b) {
		switch(vt.getState()) {
		case ST_GROUND: writeGround(vt, b); break;
		case ST_ESC:    writeEscape(vt, b); break;
		case ST_ESCINT: writeEscapeIntermediate(vt, b); break;
		case ST_CSI:    writeControlSequenceIntroducer(vt, b); break;
		case ST_CSIPAR: writeControlSequenceParam(vt, b); break;
		case ST_CSIIGN: writeControlSequenceIgnore(vt, b); break;
		case ST_CSIINT: writeControlSequenceIntermediate(vt, b); break;
		case ST_DCS:    writeDeviceControlString(vt, b); break;
		case ST_DCSPAR: writeDeviceControlParam(vt, b); break;
		case ST_DCSIGN: writeDeviceControlIgnore(vt, b); break;
		case ST_DCSINT: writeDeviceControlIntermediate(vt, b); break;
		case ST_DCSPAS: writeDeviceControlStringPassThrough(vt, b); break;
		case ST_OSC:    writeOperatingSystemCommand(vt, b); break;
		case ST_APC:    writeApplicationProgramCommand(vt, b); break;
		}
	}
}


