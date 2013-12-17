qy_cgf
======
This project provides a 3GPP-compliant Charging Gateway Function (CGF) (3GPP TS 32.295, TS 32.298 R9).

The project consist of 3 components:

	GTP' protocol stack:
		incorporating gtpprime-api(https://code.google.com/p/gtpprime/): support CDR transportation messages defined in 32.295
		based on Netty3(netty.io)
	
	ASN.1 encoder/decoder:
		incorporating asn1forj(http://asn1forj.sourceforge.net/): a simple ASN.1 framework supporting serialization/deserialization between ASN.1 message (currently support BER coding) and ASN.1 datatypes.
		BER coding utilities helps to manipulate ASN.1 datatypes.

	CDR processor (using state pattern):
		perform CDR content merging tasks according to specific requirements.
		currently support PGW-CDR merging.
