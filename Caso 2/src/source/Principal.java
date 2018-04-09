package source;

import java.security.Security;
import java.security.cert.CertificateException;

public class Principal {
	public static final String PROVIDER= "BC";

	public static void main(String[] arg0) throws CertificateException
	{
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}
}
