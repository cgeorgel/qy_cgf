package com.baoyun.subsystems.cgf.asn1;

/**
 * <pre>
 * ServingNodeType ::= ENUMERATED
 * {
 * 	sGSN    (0),
 * 	pMIPSGW (1),
 * 	gTPSGW  (2),
 * 	ePDG    (3),
 * 	hSGW    (4),
 * 	mME     (5)
 * }
 * </pre>
 *
 * @author George
 *
 */
public enum ServingNodeType {

	SGSN("sGSN", 0),
	PMIPSGW("pMIPSGW", 1),
	GTPSGW("gTPSGW", 2),
	EPDG("ePDG", 3),
	HSGW("hSGW", 4),
	MME("mME", 5),
	TWAN("tWAN", 6);

	private final String stdLiteral;

	private final int value;

	private ServingNodeType(String stdLiteral, int value) {

		this.stdLiteral = stdLiteral;
		this.value = value;
	}

	public static ServingNodeType valueFor(String stdLiteral) {

		if (stdLiteral.equals("sGSN")) {

			return SGSN;
		} else if (stdLiteral.equals("pMIPSGW")) {

			return PMIPSGW;
		} else if (stdLiteral.equals("gTPSGW")) {

			return GTPSGW;
		} else if (stdLiteral.equals("ePDG")) {

			return EPDG;
		} else if (stdLiteral.equals("hSGW")) {

			return HSGW;
		} else if (stdLiteral.equals("mME")) {

			return MME;
		} else if (stdLiteral.equals("tWAN")) {

			return TWAN;
		} else {

			throw new IllegalArgumentException("unknown enum stdLiteral: " + stdLiteral
					+ " of enum type: " + ServingNodeType.class.getName());
		}
	}

	public static ServingNodeType valueFor(int value) {

		switch (value) {
			case 0:
				return SGSN;

			case 1:
				return PMIPSGW;

			case 2:
				return GTPSGW;

			case 3:
				return EPDG;

			case 4:
				return HSGW;

			case 5:
				return MME;

			case 6:
				return TWAN;

			default:
				throw new IllegalArgumentException("unknown enum value: " + value
						+ " of enum type: " + ServingNodeType.class.getName());
		}
	}

	/**
	 * @return the stdLiteral
	 */
	public String getStdLiteral() {

		return stdLiteral;
	}

	/**
	 * @return the value
	 */
	public int getValue() {

		return value;
	}
}
