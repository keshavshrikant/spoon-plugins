/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hpccsystems.pentaho.job.eclexecute;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
//import org.hpccsystems.javaecl.Output;
import org.hpccsystems.javaecl.EclDirect;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.compatibility.Value;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;
import org.hpccsystems.javaecl.Column;
import java.io.*;

import org.hpccsystems.javaecl.ECLSoap;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.core.*;
import org.pentaho.di.core.gui.SpoonFactory;

import org.pentaho.di.plugins.perspectives.eclresults.*;

import org.hpccsystems.eclguifeatures.*;
import org.pentaho.di.job.JobMeta;
import org.hpccsystems.ecljobentrybase.*;
import org.hpccsystems.pentaho.job.eclexecute.RenderWebDisplay;
import org.hpccsystems.recordlayout.RecordBO;
import org.hpccsystems.recordlayout.RecordList;
import org.hpccsystems.salt.hygiene.Generate;
import org.hpccsystems.saltui.hygiene.*;


/**
 *
 * @author ChambersJ
 */
public class ECLExecute extends ECLJobEntry{//extends JobEntryBase implements Cloneable, JobEntryInterface {
    
    private String attributeName = "";
    private String fileName = "";
    private String serverAddress = "";
    private String serverPort = "";
    private String debugLevel = "";
    private String error = "";
    
    public static boolean isReady = false;
    boolean isValid = true;


    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }
    
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public static boolean isIsReady() {
        return isReady;
    }

    public static void setIsReady(boolean isReady) {
        ECLExecute.isReady = isReady;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }    

    public String getDebugLevel() {
		return debugLevel;
	}

	public void setDebugLevel(String debugLevel) {
		this.debugLevel = debugLevel;
	}
	 
	
	public boolean compileSalt(String saltPath, String spcFile, String outputDir, String jobNameNoSpace){
		boolean saltExists = (new File(saltPath + "\\salt.exe")).exists();
		boolean outExists = (new File(outputDir).exists());
		boolean success = false;
		if(saltExists && outExists){
			String cmd = "\"" + saltPath + "\\salt.exe\" -ga -D\"" + outputDir + "\" \"" + spcFile + "\"";
			//System.out.println("-->" + cmd + "<--");
		 	try{
				 System.out.println("runtime");
				 File path = new File(saltPath);
				 Runtime rt = java.lang.Runtime.getRuntime();
				 Process p = rt.exec(cmd, null, path);
				 
				//block until salt compile is completed
	                int b = 0;
	                while(!(new File(this.fileName + "\\" + jobNameNoSpace + "module")).exists() && b < 150){
	                	Thread.sleep(1000);
	                	b++;
	                }
	                
				// if(p.exitValue() == 0){
					 if((new File(this.fileName + "\\" + jobNameNoSpace + "module")).exists()){
						 success = true;
					 }
				// }
				 
				 
                    
		 	}catch (Exception e){
	            System.out.println(e.toString());
	            e.printStackTrace();
	            error += "Couldn't Locate the Salt Install and/or the output Directory Please Verify settings!";
	            return false;
	        }
		}else{
			//salt install doesn't exist
			if(!saltExists){error += "Salt not found in provided path, please check Global Variables! \r\n";}
			if(!outExists){error += "Output Directory defined in Execute step doesn't exist";}
			success = false;
		}
		
		return success;
	}
	 
	public void fixEclFiles(String dir){
		File folder = new File(dir);
		if(folder.exists()){
	        File[] listOfFiles = folder.listFiles();
	
	        for (int i = 0; i < listOfFiles.length; i++) {
	
	            if (listOfFiles[i].isFile()) {
	            	String name = listOfFiles[i].getName();
	            	System.out.println("file_rename==========: " + listOfFiles[i].getName());
	            	System.out.println(name.substring((name.length())-3, (name.length())));
					if(!name.substring((name.length())-3, (name.length())).equalsIgnoreCase("ecl")){
		                File f = new File(dir+"\\"+listOfFiles[i].getName()); 
		                System.out.println("file_rename==========: " + listOfFiles[i].getName());
		                String newName = listOfFiles[i].getName() + ".ecl";
		                f.renameTo(new File(dir + "\\"+ newName));
					}
	            }
	        }
		}

	}
	@Override
    public Result execute(Result prevResult, int k) throws KettleException {
		
        
		String resName = "";
      //Result result = null;
		//String xmlBuilder = "";
		String xmlHygieneBuilder = "";
		String layoutECL = "";
        
        Result result = prevResult;
        if(result.isStopped()){
            logBasic("{Output Job is Stopped}");

           if(!ECLExecute.isReady){
             //  result.setResult(false);
           } 
        }else{
        
	
	        JobMeta jobMeta = super.parentJob.getJobMeta();
	                
	        String serverHost = "";
	        String serverPort = "";
	        String cluster = "";
	        String jobName = "";
	        String jobNameNoSpace ="";
	        String maxReturn = "";
	        String eclccInstallDir = "";
	        String mlPath = "";
	        String includeML = "";
	        String user = "";
	        String pass = "";
	        String dsECL = "";
	        

            String rsDef = "";
	        String dsDef = "";
	        String recordName = "";
	        String dsName = "";
	        String logicalFile = "";
	        String fileType = "";
	        
	        String SALTPath = "";
	        String includeSALT = "";
	        String saltIncludePath = "";
	        Boolean isInternalLinking = false;
	        
	        AutoPopulate ap = new AutoPopulate();
	        try{
	        //Object[] jec = this.jobMeta.getJobCopies().toArray();
	
	            serverHost = ap.getGlobalVariable(jobMeta.getJobCopies(),"server_ip");
	            serverPort = ap.getGlobalVariable(jobMeta.getJobCopies(),"server_port");
	
	            cluster = ap.getGlobalVariable(jobMeta.getJobCopies(),"cluster");
	            jobName = ap.getGlobalVariable(jobMeta.getJobCopies(),"jobName");
	            jobNameNoSpace = jobName.replaceAll("[^A-Za-z0-9]", "");//jobName.replace(" ", "_"); 
	            maxReturn = ap.getGlobalVariable(jobMeta.getJobCopies(),"maxReturn");
	            eclccInstallDir = ap.getGlobalVariable(jobMeta.getJobCopies(),"eclccInstallDir");
	            mlPath = ap.getGlobalVariable(jobMeta.getJobCopies(),"mlPath");
	            includeML = ap.getGlobalVariable(jobMeta.getJobCopies(),"includeML");
	            user = ap.getGlobalVariable(jobMeta.getJobCopies(),"user_name");
                pass = ap.getGlobalVariableEncrypted(jobMeta.getJobCopies(),"password");

                isInternalLinking = ap.hasNodeofType(jobMeta.getJobCopies(), "SaltInternalLinking");
	            
	            SALTPath = ap.getGlobalVariable(jobMeta.getJobCopies(),"SALTPath");
	            includeSALT = ap.getGlobalVariable(jobMeta.getJobCopies(),"includeSALT");
	            
	            //System.out.println("@@@@@@@@@@@@@@@@@@@" + includeML + "@@@@@@");
	
	        }catch (Exception e){
	            System.out.println("Error Parsing existing Global Variables ");
	            System.out.println(e.toString());
	            e.printStackTrace();
	
	        }
	        
	      //insert code here to build spec file on compile
	        if(includeSALT.equalsIgnoreCase("true")){
	        	saltIncludePath = this.fileName+ "";
	        }
	        if(includeSALT.equalsIgnoreCase("true") && !isInternalLinking){
	        System.out.println("----------- insert code here to build spec file on compile");
		        try{
		        	//find all the datasets and build xml files
		        	String[] datasets = ap.parseDatasets(jobMeta.getJobCopies());
		        	
		        	if(datasets.length != 1){
		        		//set error state WE ONLY ALLOW ONE DATASET
		        		
		        	}
		        	String file_name = "";
		        	
		        	for(int i = 0; i < datasets.length; i++){
		        		 //iterate through all the xml files and build a specification file.
		        		//System.out.println("dataset: " + datasets[i]);
		        		RecordList fields = ap.rawFieldsByDataset(datasets[i],jobMeta.getJobCopies());
		        		//have field declaration now we need to build the xml
		        		String fieldCSV = "";
		        		for (Iterator<RecordBO> iterator = fields.getRecords().iterator(); iterator.hasNext();) {
		        			RecordBO obj = (RecordBO) iterator.next();
		 
		        			//TODO: fix this so it uses the ID passed in Hygine
		        			if(!obj.getColumnName().equalsIgnoreCase("spoonClusterID")){
		        				if(fieldCSV.equals("")){
		        					fieldCSV += obj.getColumnName();
		        				}else{
		        					fieldCSV += ","+obj.getColumnName();
		        				}
		        				xmlHygieneBuilder += buildHygieneRule(datasets[i], obj.getColumnName(),obj.getColumnType());
		        			}
		        			if(obj.getColumnName().equalsIgnoreCase("spoonClusterID")){
		        				//xmlHygieneBuilder +="<hyg:idname>" + "spoonClusterID" + "</hyg:idname>" +"\r\n";
		        			}
		        			//xmlHygieneBuilder +="<hyg:ridfield>" + "spoonRecordID" + "</hyg:hyg:ridfield>" +"\r\n";
		        			
		        		}
		        		xmlHygieneBuilder +="<hyg:idname>" + "spoonClusterID" + "</hyg:idname>" +"\r\n";
		        		//jobMeta.getJob
		        		file_name = ap.getDatasetsField("record_name", datasets[i],jobMeta.getJobCopies());
		        		
		        		//todo: write layout_<file_name> to file needed for soap
		        		layoutECL = "EXPORT layout_" + file_name + " := RECORD\r\n" + resultListToString(fields);
		        		layoutECL += "UNSIGNED6 spoonClusterID := 0;\r\n";
		        		layoutECL += "UNSIGNED6 spoonRecordID := 0;\r\n";
		        		layoutECL += "\r\nEND;\r\n\r\n";
		        		//xmlHygieneBuilder += buildHygieneRule(datasets[i], "SRC","");
		        		//xmlHygieneBuilder += "<hyg:sourcefield>" + fieldCSV + "</hyg:sourcefield>";
		        		xmlHygieneBuilder += buildOptReports(datasets[i]);
		        		//dsECL += System.getProperties().getProperty("Dataset-" + datasets[i]);
	
		    	        
		    	        rsDef = System.getProperties().getProperty("Dataset-" + datasets[i]+"-rsDef");
		    	        dsDef = System.getProperties().getProperty("Dataset-" + datasets[i]+"-dsDef");
		    	        recordName = System.getProperties().getProperty("Dataset-" + datasets[i]+"-rs");
		    	        logicalFile = System.getProperties().getProperty("Dataset-" + datasets[i]+"-logical");
		    	        fileType = System.getProperties().getProperty("Dataset-" + datasets[i]+"-type");
		    	        dsName = System.getProperties().getProperty("Dataset-" + datasets[i]+"-ds");
		        	}
		        	
		        	
		        	
		        	
		        	//need to load the hygine data if it exists
		        	
		        	/*
		        	xmlBuilder = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\r\n" +
		        				"<salt-specification xmlns=\"http://hpccsystems.com/SALT\">\r\n" +
		        				"<module-name>" + jobNameNoSpace + "module</module-name>\r\n" +
		        				"<file-name>" + file_name + "</file-name>\r\n" +
		        				"<fields>\r\n" + xmlBuilder +
		        				"</fields>\r\n" +
		        				"</salt-specification>";
		        	*/
		        	//write file to output directory				
		
					
		        	xmlHygieneBuilder = "<hyg:hygiene-spec xmlns:hyg=\"http://hpccsystems.org/salt/hygiene/bean\">" +"\r\n"+
										    "<hyg:module-name>" + jobNameNoSpace + "module</hyg:module-name>" +"\r\n"+
										    "<hyg:file-name>" + file_name + "</hyg:file-name>" +"\r\n"+
										    "<hyg:dataset-rsdef><![CDATA[" + rsDef + "]]></hyg:dataset-rsdef>" +"\r\n"+
										    "<hyg:dataset-dsdef><![CDATA[" + dsDef + "]]></hyg:dataset-dsdef>" +"\r\n"+
										    "<hyg:dataset-record-name><![CDATA[" + recordName + "]]></hyg:dataset-record-name>" +"\r\n"+
										    "<hyg:dataset-name><![CDATA[" + dsName + "]]></hyg:dataset-name>" +"\r\n"+
										    "<hyg:dataset-logical-file><![CDATA[" + logicalFile + "]]></hyg:dataset-logical-file>" +"\r\n"+
										    "<hyg:dataset-file-type><![CDATA[" + fileType + "]]></hyg:dataset-file-type>" +"\r\n"+
										    xmlHygieneBuilder +
										    "</hyg:hygiene-spec>";
		        	//System.out.println("-------------------------------------SALT COMPILE--------------------------------");
		        	try {              
		             
		                
		                BufferedWriter out = new BufferedWriter(new FileWriter(this.fileName + "\\salt.xml"));
		                out.write(xmlHygieneBuilder);
		                out.close();
		                
		                //FileInputStream fis = new FileInputStream(
		               // 		this.fileName + "\\salt.xml");
		                //need to compare xml bevore writting it to see if need to re-compile salt
		        		Generate gen = new Generate();
		        		String spec = gen.generateHygieneSpecFromXMLFile(this.fileName + "\\salt.xml");
		        		BufferedWriter out2 = new BufferedWriter(new FileWriter(this.fileName + "\\salt.spc"));
		                out2.write(spec);
		                out2.close();
		                
		                String modFile = "";
		                //System.out.println("-------------------------------------SALT COMPILE2--------------------------------");
		                boolean compileSuccess = compileSalt(SALTPath, this.fileName + "\\salt.spc", this.fileName+ "",jobNameNoSpace);
		                
		                if(!compileSuccess){
		                	 String SaltError = "Unable to create the SALT files! Please check your salt path in Global Variables, and your output path in Execute.";
		                	 result.setResult(false);
		                	 result.setStopped(true);
		                	 result.setLogText(SaltError);
		                 	 logError(SaltError);
		                 	 System.out.println(SaltError);
		                 	 error += SaltError;
		                 	 return result;
		                }else{
		                	logBasic("Salt Compiled");
		                	//System.out.println("Salt Compiled moving on");
		                }
		                
		                //fixEclFiles(this.fileName + "\\" + jobNameNoSpace + "module");
		                
		        		
		            } catch (IOException e) {
		                 e.printStackTrace();
		                 String SaltError = "Unable to create the SALT files! Please check your salt path in Global Variables, and your output path in Execute.";
	                	 result.setResult(false);
	                	 result.setStopped(true);
	                	 result.setLogText(SaltError);
	                 	 logError(SaltError);
	                 	 System.out.println(SaltError);
	                 	 error += SaltError;
	                 	 return result;
		            }   
		        	
		        	
		        	try {              

		                BufferedWriter out = new BufferedWriter(new FileWriter(this.fileName + "\\" + jobNameNoSpace + "module\\layout_" + file_name + ".ecl"));
		                out.write(layoutECL);
		                out.close();
		            } catch (IOException e) {
		                 e.printStackTrace();
		            }   
		        	
		        }catch (Exception e){
		        	System.out.println("--------------FAILED---------------");
		        	System.out.println(e.toString());
		            e.printStackTrace();
		        }
	        }
	        
	        //System.out.println("Output -- Finished setting up Global Variables");
	        this.setServerAddress(serverHost);
	        this.setServerPort(serverPort);
        
        
        
            ECLExecute.isReady=true;
            //logBasic("not waiting: " + ECLExecute.isReady);
            result.setResult(true);


            List<RowMetaAndData> list = result.getRows();
            String eclCode = "";
            if (list == null) {
                list = new ArrayList<RowMetaAndData>();
            } else {

                for (int i = 0; i < list.size(); i++) {
                    RowMetaAndData rowData = (RowMetaAndData) list.get(i);
                    String code = rowData.getString("ecl", null);
                    if (code != null) {
                        eclCode += code;
                    }
                }
                logBasic("{Execute Job} Execute Code =" + eclCode);
            }
          
            
            
            EclDirect eclDirect = new EclDirect(this.serverAddress, cluster, this.serverPort);
            eclDirect.setEclccInstallDir(eclccInstallDir);
            eclDirect.setIncludeML(includeML);
            eclDirect.setJobName(jobName);
            eclDirect.setMaxReturn(maxReturn);
            eclDirect.setMlPath(mlPath);
            eclDirect.setOutputName(this.getName());
            eclDirect.setUserName(user);
            eclDirect.setPassword(pass);
            //ArrayList dsList = null;
          
            //String outStr = "";
            
            eclDirect.setIncludeSALT(includeSALT);
            eclDirect.setSALTPath(SALTPath);
            eclDirect.setSaltLib(saltIncludePath);
            ArrayList dsList = null;
            String outStr = "";
            //System.out.println("Output -- Finished setting up ECLDirect");
            try{
                String includes = "";
                includes += "IMPORT Std;\n";
                if(includeML.equalsIgnoreCase("true")){
                    includes += "IMPORT * FROM ML;\r\n\r\n";
                    includes += "IMPORT * FROM ML.Cluster;\r\n\r\n";
                    includes += "IMPORT * FROM ML.Types;\r\n\r\n";
                }
               // System.out.println("Execute -- Finished Imports");
                
                if(includeSALT.equalsIgnoreCase("true")){
                    includes += "IMPORT SALT25;\r\n\r\n";
                    includes += "IMPORT ut;\r\n\r\n";
                    includes += "IMPORT " +jobNameNoSpace + "module;\r\n\r\n";
                }
                
                System.out.println("Execute -- Finished Imports");
                eclCode = includes + eclCode;
                
                boolean isValid = false;
               // System.out.println("---------------- submitToCluster");

                isValid = eclDirect.execute(eclCode, this.debugLevel);
               // System.out.println("---------------- finished submitToCluster");

                //System.out.println("---------------- finished submitToCluster");
                if(isValid){
                	//System.out.println("---------------- writing file");
                //	System.out.println("---------------- writing file");
                	isValid = eclDirect.writeResultsToFile(this.fileName);
                }
                if(!isValid){
                	result.setResult(false);
                    result.setLogText(eclDirect.getError());
                	logError(eclDirect.getError());
                	System.out.println(eclDirect.getError());
                	error += eclDirect.getError();
                }
                    
             }catch (Exception e){
                logError("Failed to execute code on Cluster." + e);
                result.setResult(false);
                error += "Exception occured please verify all settings.";
                e.printStackTrace();
            }
            
            result.clear();
            RowMetaAndData data = new RowMetaAndData();
            data.addValue("eclErrorCode", Value.VALUE_TYPE_STRING,eclCode+"\r\n");
            //list.add(data);
            
            //data = new RowMetaAndData();
            data.addValue("eclError", Value.VALUE_TYPE_STRING, error + "\r\n");
            list.add(data);
            result.setRows(list);
            //write the executed ecl code to file
            try {              
                System.getProperties().setProperty("eclCodeFile",fileName);
                
                BufferedWriter out = new BufferedWriter(new FileWriter(this.fileName + "\\eclcode.txt"));
                out.write(eclCode);
                out.close();
            } catch (IOException e) {
                 e.printStackTrace();
            }   
             
            
            ArrayList<String> resultNames = eclDirect.getResultNames();
            for (int n = 0; n<resultNames.size(); n++){
            	System.out.println("+++Results --------------------" + resultNames.get(n));
            	resName = resultNames.get(n);
            
	            //resName = eclDirect.getResName();
	            System.out.println("++Spring HTML -------------------------" + resName);
	            if(resName.equalsIgnoreCase("dataProfilingResults") || resName.equalsIgnoreCase("Dataprofiling_AllProfiles")){ 
	                RenderWebDisplay rwd = new RenderWebDisplay();
	         		rwd.processFile(this.fileName + "\\" + resName + ".csv", this.fileName);
	         		
	         		String saltData = System.getProperty("saltData");
	         		if(saltData!= null && !saltData.equals("")){
	         			System.setProperty("saltData",  saltData + "," + this.fileName + "\\" + resName + ".csv");
	         		}else{
	         			System.setProperty("saltData",  this.fileName + "\\" + resName + ".csv");
	         		}
	            }
	            
	            cacheOutputInfo("Dataprofiling_SummaryReport","saltSummaryData", resName);
	            
	            /*if(resName.equalsIgnoreCase("Dataprofiling_SummaryReport")){
	            
	            	String saltSummaryData = System.getProperty("saltSummaryData");
	         		if(saltSummaryData != null && !saltSummaryData.equals("")){
	         			System.setProperty("saltSummaryData",  saltSummaryData + "," + this.fileName + "\\" + resName + ".csv");
	         		}else{
	         			System.setProperty("saltSummaryData",  this.fileName + "\\" + resName + ".csv");
	         		}
	            }*/
	            //Dataprofiling_OptimizedLayout
	            //saltOptimizedLayouts
	            cacheOutputInfo("Dataprofiling_OptimizedLayout","saltOptimizedLayouts", resName);
	            /*
	            if(resName.equalsIgnoreCase("Dataprofiling_OptimizedLayout")){
		            
	            	String saltSummaryData = System.getProperty("saltOptimizedLayouts");
	         		if(saltSummaryData != null && !saltSummaryData.equals("")){
	         			System.setProperty("saltOptimizedLayouts",  saltSummaryData + "," + this.fileName + "\\" + resName + ".csv");
	         		}else{
	         			System.setProperty("saltOptimizedLayouts",  this.fileName + "\\" + resName + ".csv");
	         		}
	            }
	            */
	         
	            //SrcOutliers
	            cacheOutputInfo("SrcOutliers","saltSrcOutliers", resName);
	            //ClusterSrc
	            cacheOutputInfo("ClusterSrc","saltClusterSrc", resName);
	            //ClusterCounts
	            cacheOutputInfo("ClusterCounts","saltClusterCounts", resName);
	            //SrcProfiles
	            cacheOutputInfo("SrcProfiles","saltSrcProfiles", resName);
	            //Hygiene_ValidityErrors
	            cacheOutputInfo("Hygiene_ValidityErrors","saltHygiene_ValidityErrors", resName);
	            //CleanedData
	            cacheOutputInfo("CleanedData","saltCleanedData", resName);
            }
        
       }
        
        
       return result;
    }
	
	public void cacheOutputInfo(String thisResName,String propertyName, String resName){
		try{
		if(resName.equalsIgnoreCase(thisResName)){
            
        	String resultData = System.getProperty(propertyName);
     		if(resultData != null && !resultData.equals("")){
     			System.setProperty(propertyName,  resultData + "," + this.fileName + "\\" + resName + ".csv");
     		}else{
     			System.setProperty(propertyName,  this.fileName + "\\" + resName + ".csv");
     		}
        }
		}catch (Exception e){
			System.out.println("Error Setting result data");
		}
	}
	
	
	public String buildOptReports(String datasetName){
		String xml = "";
		JobMeta jobMeta = super.parentJob.getJobMeta();
        List<JobEntryCopy> jobs = jobMeta.getJobCopies();
        try{
	        SaltAutoPopulate sap = new SaltAutoPopulate();
	        HashMap hm = sap.getHygine(jobs, datasetName);
	        if(hm.get("srcField") != null){
	        	xml += "<hyg:sourcefield>" + hm.get("srcField") + "</hyg:sourcefield>";
	        }
        }catch (Exception e){}    
		return xml;
	}
    
	public String buildHygieneRule(String datasetName, String columnName, String columnType){
		String xml = "";
        JobMeta jobMeta = super.parentJob.getJobMeta();
        List<JobEntryCopy> jobs = jobMeta.getJobCopies();
        
		xml += "<hyg:field-rule>"+"\r\n";
		xml += "    <hyg:field-name>" + columnName + "</hyg:field-name>"+"\r\n";
		xml += "    <hyg:field-type>" + columnType + "</hyg:field-type>"+"\r\n";
		
		//see if hygine rule exists for field
		//if rule exists build it
		try{
	        SaltAutoPopulate sap = new SaltAutoPopulate();
	        String[] rules = sap.getRule(jobs,datasetName, columnName);
	        HygieneEntryList hel = sap.getEntryList();
	        ArrayList<HygieneEntryBO> entries = hel.getEntries();
	        
	        for(int i =0; i<entries.size(); i++){
	        	HygieneEntryBO entry = entries.get(i);
	        	if(columnName.equalsIgnoreCase(entry.getField())){
	        		System.out.println("Field with Rule: " + entry.getField());
	        		//we have a column match and it has a rule 
	        		//look up rule
	        		HygieneRuleBO rule = sap.fieldTypeList.get(sap.fieldTypeList.getIndex(entry.getRuleName()));
	        		//we have the rule time to build the xml
	        		if(rule.isCaps()){
	        			xml += "    <hyg:caps>true</hyg:caps>"+"\r\n";
	        		}
	        		if(rule.isLefttrim()){
	        			xml += "    <hyg:left-trim>true</hyg:left-trim>"+"\r\n";
	        		}
	        		if(!rule.getAllow().equals("")){
	        			xml += "    <hyg:allow>"+rule.getAllow()+"</hyg:allow>"+"\r\n";
	        		}
	        		if(!rule.getSpaces().equals("")){
	        			xml += "    <hyg:spaces>"+rule.getSpaces()+"</hyg:spaces>"+"\r\n";
	        		}
	        		if(!rule.getOnfail().equals("")){
	        			xml += "    <hyg:on-fail>"+rule.getOnfail()+"</hyg:on-fail>"+"\r\n";
	        		}
	        		if(!rule.getIgnore().equals("")){
	        			//not implemented
	        			xml += "    <hyg:ignore>"+rule.getIgnore()+"</hyg:ignore>"+"\r\n";
	        		}
	        		if(!rule.getLengths().equals("")){
	        			//not implemented
	        			xml += "    <hyg:lengths>"+rule.getLengths()+"</hyg:lengths>"+"\r\n";
	        		}
	        		if(!rule.getDisallowedQuotes().equals("")){
	        			//not implemented
	        			xml += "    <hyg:disallowed-quotes>"+rule.getDisallowedQuotes()+"</hyg:disallowed-quotes>"+"\r\n";
	        		}
	        		//if(!rule.getLike().equals("")){
	        			//not implemented
	        		//	xml += "    <hyg:like>"+rule.getLike()+"</hyg:like>"+"\r\n";
	        		//}
	        		if(!rule.getWords().equals("")){
	        			//not implemented
	        			xml += "    <hyg:words>"+rule.getWords()+"</hyg:words>"+"\r\n";
	        		}
	        		
	        		
	        	}
	        }
        
        }catch (Exception e){
        	System.out.println("~~~~~~~~~~~~~~~~~~~~ERROR~~~~~~~~~~~~~~~~~~~~~~~~~~");
        	System.out.println (e.toString());

        }
		//Close off the XML Tag
		xml += "</hyg:field-rule>"+"\r\n";
		
		return xml;
	}
     
    @Override
    public void loadXML(Node node, List<DatabaseMeta> list, List<SlaveServer> list1, Repository rpstr) throws KettleXMLException {
        try {
             //System.out.println(" ------------ loadXML ------------- ");
            super.loadXML(node, list, list1);
            if(XMLHandler.getNodeValue(XMLHandler.getSubNode(node, "file_name")) != null)
                setFileName(XMLHandler.getNodeValue(XMLHandler.getSubNode(node, "file_name")));
            if(XMLHandler.getNodeValue(XMLHandler.getSubNode(node, "debugLevel")) != null)
                setDebugLevel(XMLHandler.getNodeValue(XMLHandler.getSubNode(node, "debugLevel")));

        } catch (Exception e) {
            e.printStackTrace();
            throw new KettleXMLException("ECL Output Job Plugin Unable to read step info from XML node", e);
        }

    }

    public String getXML() {
        String retval = "";
        // System.out.println(" ------------ getXML ------------- ");
        retval += super.getXML();
        retval += "		<file_name><![CDATA[" + fileName + "]]></file_name>" + Const.CR;
        retval += "		<debugLevel><![CDATA[" + this.debugLevel + "]]></debugLevel>" + Const.CR;
        return retval;

    }

    public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers)
            throws KettleException {
        //System.out.println(" ------------ loadRep " + id_jobentry + "------------- ");
        try {
           if(rep.getStepAttributeString(id_jobentry, "fileName") != null)
                fileName = rep.getStepAttributeString(id_jobentry, "fileName"); //$NON-NLS-1$
            if(rep.getStepAttributeString(id_jobentry, "debugLevel") != null)
                debugLevel = rep.getStepAttributeString(id_jobentry, "debugLevel"); //$NON-NLS-1$
        
        } catch (Exception e) {
            throw new KettleException("Unexpected Exception", e);
            
        }
    }

    public void saveRep(Repository rep, ObjectId id_job) throws KettleException {
       // System.out.println(" ------------ saveRep " + id_job + " ------------- ");
        try {
             
            ObjectId[] allIDs = rep.getPartitionSchemaIDs(true);
            for(int i = 0; i<allIDs.length; i++){
                logBasic("ObjectID["+i+"] = " + allIDs[i]);
            }
            rep.saveStepAttribute(id_job, getObjectId(), "fileName", fileName); //$NON-NLS-1$
            rep.saveStepAttribute(id_job, getObjectId(), "debugLevel", this.debugLevel); //$NON-NLS-1$
           
        } catch (Exception e) {
            throw new KettleException("Unable to save info into repository" + id_job, e);
        }
    }

    public boolean evaluates() {
    	return isValid;
    }

    public boolean isUnconditional() {
        return false;
    }
    
    
    
    
    
   
}
