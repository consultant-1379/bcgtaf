package com.ericsson.bcgcdb.test.cases;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.inject.Inject;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import org.testng.annotations.Test;

import com.ericsson.bcgcdb.test.operators.ImportStkpiOperator;
import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;

public class importStkpiRelationCreation extends TorTestCaseHelper implements TestCase {

	@Inject
	private OperatorRegistry<ImportStkpiOperator> bCGImportProvider;

	/**
	 * @throws IOException
	 * @DESCRIPTION BCG STKPI Import Test case For 1000 UtranRelations
	 * @PRE Export of the UtranRelation
	 * @PRIORITY HIGH
	 */
	@TestId(id="OSS-48697_STKPI_Import_2", title = "BCG STKPI Import Testcases For Creation Of 1000 UtranRelations .")
	@Context(context = {Context.CLI})
	@Test(groups={"STCDB","KGB","CDB"})
	@DataDriven(name = "ImportStkpiRelationCreation")
	public void bCGImport(
			@Input("ImportMo") String importMo,
			@Input("ImportType") String importType,
			@Input("ImportFileName") String importFileName,
			@Input("PlanName") String planName,
			@Input("modifier") String modifier,
			@Input("BaseLineMoRate") String baseLineMoRate,
			@Input("TotalMOs") int totalMOs,
			@Input("Iterations") int iterations,
			@Input("VarianceAllowed") double varianceAllowed) throws XMLStreamException, FileNotFoundException,
			FactoryConfigurationError, ParserConfigurationException, TransformerException,IOException {

		ImportStkpiOperator importStkpiOperator = bCGImportProvider.provide(ImportStkpiOperator.class);

		String deleteRelationIntermediateFile = "deleteRelationIntermediateFileFor"+totalMOs+"MOs.xml";
		double totalMORate = 0;
		double variance;
		String pattern = "#.00";
		double mimRate;
		mimRate = Double.parseDouble(baseLineMoRate);
		DecimalFormat decimalFormat = new DecimalFormat(pattern);

		try{
			importStkpiOperator.createDirectoryInTemp();

			setTestStep("Change the value of BCG PED parameter ExportNodeIfUnsynched to false ");
			importStkpiOperator.setPEDParameter();
			setSubTestStep("Verify that value of the PED parameter ExportNodeIfUnsynched is false .");
			assertFalse("PED parameter is still true ", importStkpiOperator.checkPEDParameterStatus());

			setTestStep("Export all UtranRelations from full Network ");
			String exportCommandOutput = importStkpiOperator.performMOCExport();

			setSubTestStep("Verify the output after export command execution contains success message");
			assertTrue("Export output doesn't contain success message ", exportCommandOutput.contains("Export has succeeded"));

			setSubTestStep("Verify that Exit code of Export Command is 0");
			assertEquals("Exit code for export is non-zero", 0, importStkpiOperator.actualExitValue());

			setSubTestStep("Verify that export file exists on the export path");
			assertTrue("Export file not found in export directory ", importStkpiOperator.fileExists(importStkpiOperator.EXPORTFILEPATH,importStkpiOperator.getExportFileName()));

			setTestStep("Copy Export XML from remote location to local");
			setSubTestStep("Verify file transfer completed successfully  .");
			assertTrue("Transfer of the export file did not get completed ",importStkpiOperator.copyRemoteXmlFile());

			setSubTestStep("Verify Export file size greater than 0");
			assertTrue("Export file is empty . ",importStkpiOperator.verifyFileSizeGreaterThanZero(importStkpiOperator.getExportFileName()));

			setTestStep("Create import file for deletion of 1000 UtranRelations between 2 RNCs .");
			assertTrue("Creation of import file cannot be done ",importStkpiOperator.createImportFile(modifier,totalMOs,importFileName,importType));

			setSubTestStep("Verify that the created import file exist on the local .");
			assertTrue("Import file does not exist on the local .",importStkpiOperator.verifyImportFileExistOnLocal(importFileName));

			setSubTestStep("Verify Import file size greater than 0");
			assertTrue("Import file is empty .",importStkpiOperator.verifyFileSizeGreaterThanZero(importFileName));

			setTestStep("Copy new formed Import XML from local to remote location");
			assertTrue("Transfer of Import File from Local to Remote Location was unsuccessful ",importStkpiOperator.copyLocalXmlFile(importFileName));

			setSubTestStep("Verify that import XML have "+totalMOs+" Utran Relations for deletion");
			assertTrue("Import file does not have "+totalMOs+"Utran Relations for deletion",importStkpiOperator.verifyTotalImportCommands(importFileName,modifier,totalMOs));

			setSubTestStep("Verify that Import file exists on the Import path");
			assertTrue("Import file not found in import directory ", importStkpiOperator.fileExists(importStkpiOperator.IMPORTFILEPATH,importFileName));

			setTestStep("Convert Import Create UtranRelation File to Intermediate Delete UtranRelation File");
			importStkpiOperator.convertCreateFileToDeleteFile(importFileName,deleteRelationIntermediateFile);

			setSubTestStep("Verify that the created Intermediate Delete Import file exist on the local .");
			assertTrue("Intermediate Delete Import file does not exist on the local .",importStkpiOperator.verifyImportFileExistOnLocal(deleteRelationIntermediateFile));

			setSubTestStep("Verify Intermediate Delete Import file size greater than 0");
			assertTrue("Intermediate DeleteImport file is empty .",importStkpiOperator.verifyFileSizeGreaterThanZero(deleteRelationIntermediateFile));

			setTestStep("Copy new Intermediate Delete Import XML from local to remote location");
			assertTrue("Transfer of Intermediate Delete Import File from Local to Remote Location was unsuccessful ",importStkpiOperator.copyLocalXmlFile(deleteRelationIntermediateFile));

			setSubTestStep("Verify that Import file exists on the Import path");
			assertTrue("Import file not found in import directory ", importStkpiOperator.fileExists(importStkpiOperator.IMPORTFILEPATH,deleteRelationIntermediateFile));

			setTestStep("Execute the import command for Intermediate Delete file with a plan name ");
			String importCommandOutput = importStkpiOperator.performImport(deleteRelationIntermediateFile,planName);

			setSubTestStep("Verify the output after Import command execution contains success message");
			assertTrue("Import output doesn't contain success message ", importCommandOutput.contains("Import has succeeded"));


			setSubTestStep("Verify that Exit code of Import Command is 0");
			assertEquals("Exit code for export is non-zero", 0, importStkpiOperator.actualExitValue());

			setTestStep("Execute the activation command with a plan name ");

			String activationCommandOutput = importStkpiOperator.performActivation();

			setSubTestStep("Verify the output after Activation command execution contains success message");
			assertTrue("Activation output doesn't contain success message ", activationCommandOutput.contains("Activation SUCCESSFUL for plan"));

			setSubTestStep("Verify that Exit code of Activation Command is 0");
			assertEquals("Exit code for Activation is non-zero", 0, importStkpiOperator.actualExitValue());

			setTestStep("Execute the Plan Deletion command with a plan name ");
			String deletePlanCommandOutput = importStkpiOperator.deletePlan();

			setSubTestStep("Verify the output after Plan Delete command execution contains success message");
			assertTrue("Import output doesn't contain success message ", deletePlanCommandOutput.contains("The Plan(s) have been successfully deleted"));

			setSubTestStep("Verify that Exit code of Activation Command is 0");
			assertEquals("Exit code for export is non-zero", 0, importStkpiOperator.actualExitValue());

			for(int i=1 ; i<iterations;i++){

				setTestStep("Execute the import command with a plan name ");
				importCommandOutput = importStkpiOperator.performImport(importFileName,planName);

				setSubTestStep("Verify the output after Import command execution contains success message");
				assertTrue("Import output doesn't contain success message ", importCommandOutput.contains("Import has succeeded"));


				setSubTestStep("Verify that Exit code of Import Command is 0");
				assertEquals("Exit code for export is non-zero", 0, importStkpiOperator.actualExitValue());

				setTestStep("Collect the information for the total time taken in use case from Instrumentation file and calculate MO/Sec for the use case.");
				double moRate = importStkpiOperator.verifyImportMoRate(importFileName,totalMOs);

				totalMORate = totalMORate + moRate;

				setAdditionalResultInfo("MO Rate for execution "+ i +" : "+ decimalFormat.format(moRate));

				setTestStep("Execute the Plan Deletion command with a plan name ");
				deletePlanCommandOutput = importStkpiOperator.deletePlan();

				setSubTestStep("Verify the output after Plan Delete command execution contains success message");
				assertTrue("Import output doesn't contain success message ", deletePlanCommandOutput.contains("The Plan(s) have been successfully deleted"));

				setSubTestStep("Verify that Exit code of Activation Command is 0");
				assertEquals("Exit code for export is non-zero", 0, importStkpiOperator.actualExitValue());


			}

			setTestStep("Execute the import command with a plan name ");
			importCommandOutput = importStkpiOperator.performImport(importFileName,planName);

			setSubTestStep("Verify the output after Import command execution contains success message");
			assertTrue("Import output doesn't contain success message ", importCommandOutput.contains("Import has succeeded"));


			setSubTestStep("Verify that Exit code of Import Command is 0");
			assertEquals("Exit code for export is non-zero", 0, importStkpiOperator.actualExitValue());

			setTestStep("Collect the information for the total time taken in use case from Instrumentation file and calculate MO/Sec for the use case.");
			double moRate = importStkpiOperator.verifyImportMoRate(importFileName,totalMOs);

			setAdditionalResultInfo("MO Rate for execution "+ iterations +" : "+ decimalFormat.format(moRate));
			totalMORate = totalMORate + moRate;

			final double finalMORate = totalMORate/iterations;

			setTestStep("Execute the activation command with a plan name ");
			activationCommandOutput = importStkpiOperator.performActivation();

			setSubTestStep("Verify the output after Activation command execution contains success message");
			assertTrue("Activation output doesn't contain success message ", activationCommandOutput.contains("Activation SUCCESSFUL for plan"));

			setSubTestStep("Verify that Exit code of Activation Command is 0");
			assertEquals("Exit code for Activation is non-zero", 0, importStkpiOperator.actualExitValue());

			setTestStep("Execute the Plan Deletion command with a plan name ");
			deletePlanCommandOutput = importStkpiOperator.deletePlan();

			setSubTestStep("Verify the output after Plan Delete command execution contains success message");
			assertTrue("Import output doesn't contain success message ", deletePlanCommandOutput.contains("The Plan(s) have been successfully deleted"));

			setSubTestStep("Verify that Exit code of Activation Command is 0");
			assertEquals("Exit code for export is non-zero", 0, importStkpiOperator.actualExitValue());

//			assertTrue("MO rate is less than the previous baseline ",finalMORate>=Double.parseDouble(baseLineMoRate));

			setAdditionalResultInfo("Average MO Rate for "+ iterations +" executions : "+ decimalFormat.format(finalMORate));
			setAdditionalResultInfo("Previous Base Line of MO Rate : "+ baseLineMoRate);
			variance = ((mimRate - finalMORate)*100)/mimRate;
			setAdditionalResultInfo("Actual variance = " + variance + "%");
			setAdditionalResultInfo("Variance Allowed <= " + varianceAllowed +"%");
			assertTrue((variance <= (varianceAllowed)));
		}
		finally{
			importStkpiOperator.exitCleanUpForCreation(deleteRelationIntermediateFile);
		}
	}
}

