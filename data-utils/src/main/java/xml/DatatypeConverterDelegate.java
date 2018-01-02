package xml;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.DatatypeConverterInterface;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

/**
 * 'Knap staaltje' JAXB code ... doh, blijkbaar is DatatypeConverter een static 
 * class met alleen maar static members, mooi...
 * 
 * @author mwa17610
 *
 */
public class DatatypeConverterDelegate implements DatatypeConverterInterface{
	public DatatypeConverterDelegate(){
		
	}
	@Override
	public String parseString(String lexicalXSDString) {
		return DatatypeConverter.parseString(lexicalXSDString);
	}

	@Override
	public BigInteger parseInteger(String lexicalXSDInteger) {
		return DatatypeConverter.parseInteger(lexicalXSDInteger);
	}

	@Override
	public int parseInt(String lexicalXSDInt) {
		return DatatypeConverter.parseInt(lexicalXSDInt);
	}

	@Override
	public long parseLong(String lexicalXSDLong) {
		return DatatypeConverter.parseLong(lexicalXSDLong);
	}

	@Override
	public short parseShort(String lexicalXSDShort) {
		return DatatypeConverter.parseShort(lexicalXSDShort);
	}

	@Override
	public BigDecimal parseDecimal(String lexicalXSDDecimal) {
		return DatatypeConverter.parseDecimal(lexicalXSDDecimal);
	}

	@Override
	public float parseFloat(String lexicalXSDFloat) {
		return DatatypeConverter.parseFloat(lexicalXSDFloat);
	}

	@Override
	public double parseDouble(String lexicalXSDDouble) {
		return DatatypeConverter.parseDouble(lexicalXSDDouble);
	}

	@Override
	public boolean parseBoolean(String lexicalXSDBoolean) {
		return DatatypeConverter.parseBoolean(lexicalXSDBoolean);
	}

	@Override
	public byte parseByte(String lexicalXSDByte) {
		return DatatypeConverter.parseByte(lexicalXSDByte);
	}

	@Override
	public QName parseQName(String lexicalXSDQName, NamespaceContext nsc) {
		return DatatypeConverter.parseQName(lexicalXSDQName, nsc);
	}

	@Override
	public Calendar parseDateTime(String lexicalXSDDateTime) {
		return DatatypeConverter.parseDateTime(lexicalXSDDateTime);
	}

	@Override
	public byte[] parseBase64Binary(String lexicalXSDBase64Binary) {
		return DatatypeConverter.parseBase64Binary(lexicalXSDBase64Binary);
	}

	@Override
	public byte[] parseHexBinary(String lexicalXSDHexBinary) {
		return DatatypeConverter.parseHexBinary(lexicalXSDHexBinary);
	}

	@Override
	public long parseUnsignedInt(String lexicalXSDUnsignedInt) {
		return DatatypeConverter.parseUnsignedInt(lexicalXSDUnsignedInt);
	}

	@Override
	public int parseUnsignedShort(String lexicalXSDUnsignedShort) {
		return DatatypeConverter.parseUnsignedShort(lexicalXSDUnsignedShort);
	}

	@Override
	public Calendar parseTime(String lexicalXSDTime) {
		return DatatypeConverter.parseTime(lexicalXSDTime);
	}

	@Override
	public Calendar parseDate(String lexicalXSDDate) {
		return DatatypeConverter.parseDate(lexicalXSDDate);
	}

	@Override
	public String parseAnySimpleType(String lexicalXSDAnySimpleType) {
		return DatatypeConverter.parseAnySimpleType(lexicalXSDAnySimpleType);
	}

	@Override
	public String printString(String val) {
		return DatatypeConverter.printString(val);
	}

	@Override
	public String printInteger(BigInteger val) {
		return DatatypeConverter.printInteger(val);
	}

	@Override
	public String printInt(int val) {
		return DatatypeConverter.printInt(val);
	}

	@Override
	public String printLong(long val) {
		return DatatypeConverter.printLong(val);
	}

	@Override
	public String printShort(short val) {
		return DatatypeConverter.printShort(val);
	}

	@Override
	public String printDecimal(BigDecimal val) {
		return DatatypeConverter.printDecimal(val);
	}

	@Override
	public String printFloat(float val) {
		return DatatypeConverter.printFloat(val);
	}

	@Override
	public String printDouble(double val) {
		return DatatypeConverter.printDouble(val);
	}

	@Override
	public String printBoolean(boolean val) {
		return DatatypeConverter.printBoolean(val);
	}

	@Override
	public String printByte(byte val) {
		return DatatypeConverter.printByte(val);
	}

	@Override
	public String printDateTime(Calendar val) {
		return DatatypeConverter.printDateTime(val);
	}

	@Override
	public String printBase64Binary(byte[] val) {
		return DatatypeConverter.printBase64Binary(val);
	}

	@Override
	public String printHexBinary(byte[] val) {
		return DatatypeConverter.printHexBinary(val);
	}

	@Override
	public String printUnsignedInt(long val) {
		return DatatypeConverter.printUnsignedInt(val);
	}

	@Override
	public String printUnsignedShort(int val) {
		return DatatypeConverter.printUnsignedShort(val);
	}

	@Override
	public String printTime(Calendar val) {
		return DatatypeConverter.printTime(val);
	}

	@Override
	public String printDate(Calendar val) {
		return DatatypeConverter.printDate(val);
	}

	@Override
	public String printAnySimpleType(String val) {
		return DatatypeConverter.printAnySimpleType(val);
	}

	@Override
	public String printQName(QName val, NamespaceContext nsc) {
		return DatatypeConverter.printQName(val, nsc);
	}

}
