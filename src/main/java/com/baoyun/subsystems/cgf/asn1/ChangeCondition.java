package com.baoyun.subsystems.cgf.asn1;

/**
 * <pre>
 * ChangeCondition ::= ENUMERATED
 * {
 * 	qoSChange          (0),
 * 	tariffTime         (1),
 * 	recordClosure      (2),
 * 	cGI-SAICHange      (6),
 * 	rAIChange          (7),
 * 	dT-Establishment   (8),
 * 	dT-Removal         (9),
 * 	eCGIChange         (10), -- bearer modification. ECGI Change
 * 	tAIChange          (11), -- bearer modification. TAI Change
 * 	userLocationChange (12) -- bearer modification. User Location Change
 * }
 * </pre>
 *
 * @author George
 *
 */
public enum ChangeCondition {

	QOS_CHANGE("qoSChange", 0),
	TARIFF_TIME("tariffTime", 1),
	RECORD_CLOSURE("recordClosure", 2),
	CGI_SAI_CHANGE("cGI-SAICHange", 6),
	RAI_CHANGE("rAIChange", 7),
	DT_ESTABLISHMENT("dT-Establishment", 8),
	DT_REMOVAL("dT-Removal", 9),
	ECGI_CHANGE("eCGIChange", 10),
	TAI_CHANGE("tAIChange", 11),
	USER_LOCATION_CHANGE("userLocationChange", 12);

	private int value;

	private String stdLiteral;

	ChangeCondition(String stdLiteral, int value) {

		this.stdLiteral = stdLiteral;
		this.value = value;
	}

	public static ChangeCondition valueFor(String stdLiteral) {

		if (stdLiteral.equals("qoSChange")) {

			return QOS_CHANGE;
		} else if (stdLiteral.equals("tariffTime")) {

			return TARIFF_TIME;
		} else if (stdLiteral.equals("recordClosure")) {

			return RECORD_CLOSURE;
		} else if (stdLiteral.equals("cGI-SAICHange")) {

			return CGI_SAI_CHANGE;
		} else if (stdLiteral.equals("rAIChange")) {

			return RAI_CHANGE;
		} else if (stdLiteral.equals("dT-Establishment")) {

			return DT_ESTABLISHMENT;
		} else if (stdLiteral.equals("dT-Removal")) {

			return DT_REMOVAL;
		} else if (stdLiteral.equals("eCGIChange")) {

			return ECGI_CHANGE;
		} else if (stdLiteral.equals("tAIChange")) {

			return TAI_CHANGE;
		} else if (stdLiteral.equals("userLocationChange")) {

			return USER_LOCATION_CHANGE;
		} else {

			throw new IllegalArgumentException("unknown enum stdLiteral: " + stdLiteral
					+ " of enum type: " + ChangeCondition.class.getName());
		}
	}

	public static ChangeCondition valueFor(int value) {

		switch (value) {
			case 0:
				return QOS_CHANGE;

			case 1:
				return TARIFF_TIME;

			case 2:
				return RECORD_CLOSURE;

			case 6:
				return CGI_SAI_CHANGE;

			case 7:
				return RAI_CHANGE;

			case 8:
				return DT_ESTABLISHMENT;

			case 9:
				return DT_REMOVAL;

			case 10:
				return ECGI_CHANGE;

			case 11:
				return TAI_CHANGE;

			case 12:
				return USER_LOCATION_CHANGE;

			default:
				throw new IllegalArgumentException("unknown enum value: " + value
						+ " of enum type: " + ChangeCondition.class.getName());
		}
	}

	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @return the stdLiteral
	 */
	public String getStdLiteral() {
		return stdLiteral;
	}
}
