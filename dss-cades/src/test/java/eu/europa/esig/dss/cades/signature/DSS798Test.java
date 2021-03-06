package eu.europa.esig.dss.cades.signature;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import com.google.common.base.Charsets;

import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.DSSException;
import eu.europa.esig.dss.InMemoryDocument;
import eu.europa.esig.dss.SignatureLevel;
import eu.europa.esig.dss.SignaturePackaging;
import eu.europa.esig.dss.SignatureValue;
import eu.europa.esig.dss.ToBeSigned;
import eu.europa.esig.dss.cades.CAdESSignatureParameters;
import eu.europa.esig.dss.signature.PKIFactoryAccess;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.validation.reports.SimpleReport;

public class DSS798Test extends PKIFactoryAccess {

	@Test(expected = DSSException.class)
	public void testExtendDetachedWithoutFile() throws Exception {

		DSSDocument documentToSign = new InMemoryDocument("Hello".getBytes(Charsets.UTF_8), "bin.bin");

		CAdESSignatureParameters signatureParameters = new CAdESSignatureParameters();
		signatureParameters.bLevel().setSigningDate(new Date());
		signatureParameters.setSigningCertificate(getSigningCert());
		signatureParameters.setCertificateChain(getCertificateChain());
		signatureParameters.setSignaturePackaging(SignaturePackaging.DETACHED);
		signatureParameters.setSignatureLevel(SignatureLevel.CAdES_BASELINE_B);

		CAdESService service = new CAdESService(getCompleteCertificateVerifier());

		// Level B
		ToBeSigned dataToSign = service.getDataToSign(documentToSign, signatureParameters);
		SignatureValue signatureValue = getToken().sign(dataToSign, signatureParameters.getDigestAlgorithm(), getPrivateKeyEntry());
		DSSDocument signedDocument = service.signDocument(documentToSign, signatureParameters, signatureValue);

		// Level T without detached document
		CAdESService serviceExtend = new CAdESService(getCompleteCertificateVerifier());
		serviceExtend.setTspSource(getGoodTsa());

		CAdESSignatureParameters parametersExtend = new CAdESSignatureParameters();
		parametersExtend.setSignatureLevel(SignatureLevel.CAdES_BASELINE_T);
		serviceExtend.extendDocument(signedDocument, parametersExtend);
	}

	@Test
	public void testExtendDetachedWithFile() throws Exception {

		DSSDocument documentToSign = new InMemoryDocument("Hello".getBytes(Charsets.UTF_8), "bin.bin");

		CAdESSignatureParameters signatureParameters = new CAdESSignatureParameters();
		signatureParameters.bLevel().setSigningDate(new Date());
		signatureParameters.setSigningCertificate(getSigningCert());
		signatureParameters.setCertificateChain(getCertificateChain());
		signatureParameters.setSignaturePackaging(SignaturePackaging.DETACHED);
		signatureParameters.setSignatureLevel(SignatureLevel.CAdES_BASELINE_B);

		CAdESService service = new CAdESService(getCompleteCertificateVerifier());

		// Level B
		ToBeSigned dataToSign = service.getDataToSign(documentToSign, signatureParameters);
		SignatureValue signatureValue = getToken().sign(dataToSign, signatureParameters.getDigestAlgorithm(), getPrivateKeyEntry());
		DSSDocument signedDocument = service.signDocument(documentToSign, signatureParameters, signatureValue);

		// Level T with detached document
		CAdESService serviceExtend = new CAdESService(getCompleteCertificateVerifier());
		serviceExtend.setTspSource(getGoodTsa());

		CAdESSignatureParameters parametersExtend = new CAdESSignatureParameters();
		parametersExtend.setSignatureLevel(SignatureLevel.CAdES_BASELINE_T);
		parametersExtend.setDetachedContents(Arrays.asList(documentToSign));
		DSSDocument extendedDocument = serviceExtend.extendDocument(signedDocument, parametersExtend);

		SignedDocumentValidator validator = SignedDocumentValidator.fromDocument(extendedDocument);
		validator.setCertificateVerifier(getCompleteCertificateVerifier());
		validator.setDetachedContents(Arrays.asList(documentToSign));
		Reports reports = validator.validateDocument();
		reports.print();

		SimpleReport simpleReport = reports.getSimpleReport();
		assertEquals(SignatureLevel.CAdES_BASELINE_T.toString(), simpleReport.getSignatureFormat(simpleReport.getFirstSignatureId()));
	}

	@Override
	protected String getSigningAlias() {
		return GOOD_USER;
	}

}
