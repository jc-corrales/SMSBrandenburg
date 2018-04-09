package source;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.x509.X509V3CertificateGenerator;

@SuppressWarnings("deprecation")
public class GeneradorDeCertificados
{
	/**
	 * 
	 * @param keyPair Pares de llaves. Estas pares de llaves deben ser generadas con el algoritmo RSA,
	 * de lo contrario, esta clase no sirve.
	 */
	public GeneradorDeCertificados(KeyPair keyPair)
	{
		// generate the certificate
		X509Certificate cert;
		// show some basic validation
		try
		{
			cert = generateV3Certificate(keyPair);
			cert.checkValidity(new Date());

			cert.verify(cert.getPublicKey());
		}
		catch (InvalidKeyException | NoSuchProviderException | SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(Exception e)
		{
			System.err.println("CERTIFICADO NO VÁLIDO: " + e.getMessage());
		}
		//	        System.out.println("Public key: " + keyPair.getPublic().toString());
		//	        System.out.println("Public key: " + keyPair.getPrivate().toString());
		System.out.println("valid certificate generated");
	}
	public static X509Certificate generateV3Certificate(KeyPair pair)throws InvalidKeyException, NoSuchProviderException, SignatureException
	{
		// generate the certificate
		X509V3CertificateGenerator  certGen = new X509V3CertificateGenerator();

		certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
		certGen.setIssuerDN(new X500Principal("CN=Test Certificate"));
		certGen.setNotBefore(new Date(System.currentTimeMillis() - 50000));
		certGen.setNotAfter(new Date(System.currentTimeMillis() + 50000));
		certGen.setSubjectDN(new X500Principal("CN=Test Certificate"));
		certGen.setPublicKey(pair.getPublic());
		certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

		certGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));

		certGen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));

		certGen.addExtension(X509Extensions.ExtendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));

		certGen.addExtension(X509Extensions.SubjectAlternativeName, false, new GeneralNames(new GeneralName(GeneralName.rfc822Name, "test@test.test")));

		return certGen.generateX509Certificate(pair.getPrivate(), Principal.PROVIDER);
	}

//	public static void main(String[] args) throws Exception
//	{
//		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
//		String algoritmo = "RSA";
//		KeyPairGenerator generator;
//		generator = KeyPairGenerator.getInstance(algoritmo, Principal.PROVIDER);
//		generator.initialize(1024);
//		// create the keys
//		KeyPair keyPair = generator.generateKeyPair();
//
//		// generate the certificate
//		X509Certificate cert = generateV3Certificate(keyPair);
//
//		// show some basic validation
//		cert.checkValidity(new Date());
//
//		cert.verify(cert.getPublicKey());
//		//	        System.out.println("Public key: " + keyPair.getPublic().toString());
//		//	        System.out.println("Public key: " + keyPair.getPrivate().toString());
//		System.out.println("valid certificate generated");
//
//	}
}
