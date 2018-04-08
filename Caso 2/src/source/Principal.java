package source;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class Principal {

	public static void main(String[] arg0) throws CertificateException
	{
		// this certificate does not include any extensions
	    String sCert =
	        "-----BEGIN CERTIFICATE-----\n"
	      + "MIIBMjCB3aADAgECAhB6225ckZVssEukPuvk1U1PMA0GCSqGSIb3DQEBBAUAMBox\n"
	      + "GDAWBgNVBAMTD1Jvb3RDZXJ0aWZpY2F0ZTAeFw0wMTEwMTkxNjA5NTZaFw0wMjEw\n"
	      + "MTkyMjA5NTZaMBsxGTAXBgNVBAMTEFVzZXJDZXJ0aWZpY2F0ZTIwXDANBgkqhkiG\n"
	      + "9w0BAQEFAANLADBIAkEAzicGiW9aUlUoQIZnLy1l8MMV5OvA+4VJ4T/xo/PpN8Oq\n"
	      + "WgZVGKeEp6JCzMlXEJk3TGLfpXL4Ytw+Ldhv0QPhLwIDAnMpMA0GCSqGSIb3DQEB\n"
	      + "BAUAA0EAQmj9SFHEx66JyAps3ew4pcSS3QvfVZ/6qsNUYCG75rFGcTUPHcXKql9y\n"
	      + "qBT83iNLJ//krjw5Ju0WRPg/buHSww==\n"
	      + "-----END CERTIFICATE-----";
	    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
	    ByteArrayInputStream bytes = new ByteArrayInputStream(sCert.getBytes());
	    X509Certificate certificado = (X509Certificate)certFactory.generateCertificate(bytes);
//	    certificado.checkValidity();
	    System.out.println(certificado);
//	    System.out.println("Llave: " + certificado.getPublicKey());
	    System.out.println();
	}
}
