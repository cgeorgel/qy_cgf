package com.baoyun.subsystems.cgf.asn1;

/**
 * <pre>
 * ConsolidationResult ::= ENUMERATED
 * {
 * 	normal                    (0),
 * 	abnormal                  (1),
 * 	forInterSGSNConsolidation (2),
 * 	reachLimit                (3),
 * 	onlyOneCDRGenerated       (4)
 * }
 * </pre>
 *
 * @author George
 *
 */
public enum ConsolidationResult {

	// 3GPP spec:
	NORMAL("normal", 0),
	ABNORMAL("abnormal", 1),
	FOR_INTER_SGSN_CONSOLIDATION("forInterSGSNConsolidation", 2),
	REACH_LIMIT("reachLimit", 3),
	ONLY_ONE_CDR_GENERATED("onlyOneCDRGenerated", 4),

	// application spec:
	INIT("initial", 100);

	private int value;

	private String stdLiteral;

	ConsolidationResult(String stdLiteral, int value) {

		this.stdLiteral = stdLiteral;
		this.value = value;
	}

	public static ConsolidationResult valueFor(String stdLiteral) {

		if (stdLiteral.equals("normal")) {

			return NORMAL;
		} else if (stdLiteral.equals("abnormal")) {

			return ABNORMAL;
		} else if (stdLiteral.equals("forInterSGSNConsolidation")) {

			return FOR_INTER_SGSN_CONSOLIDATION;
		} else if (stdLiteral.equals("reachLimit")) {

			return REACH_LIMIT;
		} else if (stdLiteral.equals("onlyOneCDRGenerated")) {

			return ONLY_ONE_CDR_GENERATED;
		} else if (stdLiteral.equals("initial")) { // application spec

			return INIT;
		} else {

			throw new IllegalArgumentException("unknown enum stdLiteral: " + stdLiteral
					+ " of enum type: " + ConsolidationResult.class.getName());
		}
	}

	public static ConsolidationResult valueFor(int value) {

		switch (value) {
			case 0:
				return NORMAL;

			case 1:
				return ABNORMAL;

			case 2:
				return FOR_INTER_SGSN_CONSOLIDATION;

			case 3:
				return REACH_LIMIT;

			case 4:
				return ONLY_ONE_CDR_GENERATED;

			case 100: // application spec
				return INIT;

			default:
				throw new IllegalArgumentException("unknown enum value: " + value
						+ " of enum type: " + ConsolidationResult.class.getName());
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
