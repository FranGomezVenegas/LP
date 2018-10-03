/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package functionalJava.sop;

import databases.Rdbms;
import functionalJava.user.UserProfile;
import LabPLANET.utilities.LabPLANETArray;
import LabPLANET.utilities.LabPLANETPlatform;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class UserSop {
    
    String classVersion = "0.1";

    String[] javaDocFields = new String[0];
    Object[] javaDocValues = new Object[0];
    String javaDocLineName = "";

    String tableName = "user_sop";  
    
    public void UserSop(){}

    public Object[] userSopCertifiedBySopName(Rdbms rdbm, String schemaPrefixName, String userInfoId, String sopName ) throws SQLException{    
        return userSopCertifiedBySopInternalLogic(rdbm, schemaPrefixName, userInfoId, "sop_name", sopName);        
    }
    public Object[] userSopCertifiedBySopId(Rdbms rdbm, String schemaPrefixName, String userInfoId, String sopId ) throws SQLException{
        return userSopCertifiedBySopInternalLogic(rdbm, schemaPrefixName, userInfoId, "sop_id", sopId);        
    }
    
    private Object[] userSopCertifiedBySopInternalLogic(Rdbms rdbm, String schemaPrefixName, String userInfoId, String SopIdFieldName, String SopIdFieldValue ) throws SQLException{
        LabPLANETArray labArr = new LabPLANETArray();        
        String schemaConfigName = "config";
        Object[] diagnoses = new Object[0];
        schemaConfigName = LabPLANETPlatform.buildSchemaName(schemaPrefixName, "config");
        String actionEnabledUserSopCertification = Rdbms.getParameterBundle(schemaConfigName, "actionEnabledUserSopCertification"); 
        
        UserProfile usProf = new UserProfile();
        String[] userSchemas = usProf.getAllUserSchemaPrefix(rdbm, userInfoId);
        Boolean schemaIsCorrect = false;
        for (String us: userSchemas){
            if (us.equalsIgnoreCase(schemaPrefixName)){schemaIsCorrect=true;break;}            
        }
        if (!schemaIsCorrect){
            String errorCode = "UserSop_UserWithNoRolesForThisGivenSchema";
            diagnoses = LabPLANETPlatform.trapErrorMessage( rdbm, "LABPLANET_FALSE", classVersion, errorCode, new Object[]{userInfoId, schemaPrefixName});
            diagnoses = labArr.addValueToArray1D(diagnoses, "ERROR");
            diagnoses = labArr.addValueToArray1D(diagnoses, Rdbms.getParameterBundle(schemaConfigName, "userSopCertificationLevelImage_ERROR"));
            return diagnoses;
        }
        String[] userSchema = new String[1];
        userSchema[0]=schemaPrefixName;
        
        String[] filterFieldName = new String[2];
        Object[] filterFieldValue = new Object[2];
        String[] fieldsToReturn = new String[4];

        fieldsToReturn[0] = "sop_id";
        fieldsToReturn[1] = "sop_name";
        fieldsToReturn[2] = "status";
        fieldsToReturn[3] = "light";
        filterFieldName[0]="user_id";
        filterFieldValue[0]=userInfoId;        
        filterFieldName[1]=SopIdFieldName;
        filterFieldValue[1]=SopIdFieldValue;                
        Object[][] getUserProfileFieldValues = getUserProfileFieldValues(rdbm, filterFieldName, filterFieldValue, fieldsToReturn, userSchema);   
        if ("LABPLANET_FALSE".equalsIgnoreCase(getUserProfileFieldValues[0][0].toString())){
            diagnoses = labArr.array2dTo1d(getUserProfileFieldValues);
            diagnoses = labArr.addValueToArray1D(diagnoses, "ERROR");
            return diagnoses;
        }
        if (getUserProfileFieldValues.length<=0){
            diagnoses = LabPLANETPlatform.trapErrorMessage( rdbm, "LABPLANET_FALSE", classVersion, "UserSop_SopNotAssignedToThisUser", new Object[]{SopIdFieldValue, userInfoId, schemaPrefixName});
            diagnoses = labArr.addValueToArray1D(diagnoses, "ERROR");
            diagnoses = labArr.addValueToArray1D(diagnoses, Rdbms.getParameterBundle(schemaConfigName, "userSopCertificationLevelImage_NotAssigned"));
            return diagnoses;
        }
        if (getUserProfileFieldValues[0][3].toString().contains("GREEN")){
            diagnoses = LabPLANETPlatform.trapErrorMessage( rdbm, "LABPLANET_TRUE", classVersion, "UserSop_SopNotAssignedToThisUser", 
                    new Object[]{userInfoId, SopIdFieldValue, schemaPrefixName, "current status is "+getUserProfileFieldValues[0][2].toString()+" and the light is "+getUserProfileFieldValues[0][3].toString()});
            diagnoses = labArr.addValueToArray1D(diagnoses, "PASS");
            diagnoses = labArr.addValueToArray1D(diagnoses, Rdbms.getParameterBundle(schemaConfigName, "userSopCertificationLevelImage_Certified"));
            return diagnoses;
        }
        else{
            diagnoses = LabPLANETPlatform.trapErrorMessage( rdbm, "LABPLANET_FALSE", classVersion, "UserSop_UserNotCertifiedForSop", new Object[]{userInfoId, SopIdFieldValue, schemaPrefixName});
            diagnoses = labArr.addValueToArray1D(diagnoses, "NOTPASS");
            diagnoses = labArr.addValueToArray1D(diagnoses, Rdbms.getParameterBundle(schemaConfigName, "userSopCertificationLevelImage_NotCertified"));
            return diagnoses;
        }               
    }

    public String[] getNotCompletedUserSOP(Rdbms rdbm, String userInfoId, String schemapPrefixName) throws SQLException{

        String[] userSchemas = null;
        if (schemapPrefixName.contains("ALL")){
            UserProfile usProf = new UserProfile();
            userSchemas = usProf.getAllUserSchemaPrefix(rdbm, userInfoId);
        }
        else{
            userSchemas = new String[1];
            userSchemas[0]=schemapPrefixName;
        }

        String[] filterFieldName = new String[2];
        Object[] filterFieldValue = new Object[2];
        String[] fieldsToReturn = new String[2];

        fieldsToReturn[0] = "sop_id";
        fieldsToReturn[1] = "sop_name";
        filterFieldName[0]="user_id";
        filterFieldValue[0]=userInfoId;
        filterFieldName[1]="light";
        filterFieldValue[1]="RED";
                
        Object[][] getUserProfileFieldValues = getUserProfileFieldValues(rdbm, filterFieldName, filterFieldValue, fieldsToReturn, userSchemas);   
        Integer numLines=getUserProfileFieldValues.length;

        String[]UserSchemas=new String[numLines];
        for (Integer inumLines=0;inumLines<numLines;inumLines++){                
            UserSchemas[inumLines]=getUserProfileFieldValues[inumLines][0].toString();
        }
        return UserSchemas;           
    }
    
    public Object[] _NotRequireduserSopCertifiedBySopName(Rdbms rdbm, String schemaPrefixName, String userInfoId, String sopName, String procedure, Integer procVersion ) throws SQLException{
        return _NotRequireduserSopCertifiedBySopInternalLogic(rdbm, schemaPrefixName, userInfoId, "sop_name", sopName, procedure, procVersion);        
    }
    public Object[] _NotRequireduserSopCertifiedBySopId(Rdbms rdbm, String schemaPrefixName, String userInfoId, String sopId, String procedure, Integer procVersion ) throws SQLException{
        return _NotRequireduserSopCertifiedBySopInternalLogic(rdbm, schemaPrefixName, userInfoId, "sop_id", sopId, procedure, procVersion);
    }    
    private Object[] _NotRequireduserSopCertifiedBySopInternalLogic(Rdbms rdbm, String schemaPrefixName, String userInfoId, String sopIdFieldName, String sopIdFieldValue, String procedure, Integer procVersion ) throws SQLException{
        
        Object[] diagnoses = new Object[0];
        String sopMode = "";
        Boolean certifyManagement = false;
        Boolean enableRecertification = false;
        
        Object[][] procBusinessRule = rdbm.getRecordFieldsByFilter(rdbm, "requirements", "procedure_business_rule", 
                                                        new String[]{"procedure", "version"}, new Object[]{procedure, procVersion}, 
                                                        new String[]{"sop_mode", "certify_management", "enable_recertification", "procedure"});
        
        if ("LABPLANET_FALSE".equalsIgnoreCase(procBusinessRule[0][0].toString())){return diagnoses;}
        
        sopMode = (String) procBusinessRule[0][0];
        certifyManagement = (Boolean) procBusinessRule[0][1];
        enableRecertification = (Boolean) procBusinessRule[0][2];

        if (!sopMode.equalsIgnoreCase("ALL")){
            diagnoses[0]="SOP_DISABLE";
            diagnoses[1]="SOP disabled.";
            diagnoses[2]="xf133@FontAwesome";
            diagnoses[3]="SOPs disabled";
            return diagnoses;
        }
        
        if (!certifyManagement){
            diagnoses[0]="SOP_ENABLE_";
            diagnoses[1]="SOP enable but Certifications disabled, SOP merely info";
            diagnoses[2]="xf272@FontAwesome";
            diagnoses[3]="SOP enable but Certifications disabled, SOP merely info";
            return diagnoses;
        }
        
        UserProfile usProf = new UserProfile();
        String[] userSchemas = usProf.getAllUserSchemaPrefix(rdbm, userInfoId);
        Boolean schemaIsCorrect = false;
        for (String us: userSchemas){
            if (us.equalsIgnoreCase(schemaPrefixName)){schemaIsCorrect=true;break;}            
        }
        if (!schemaIsCorrect){
            diagnoses[0]="ERROR";
            diagnoses[1]="The user "+userInfoId+" has no roles assigned for working on schema"+schemaPrefixName;
            diagnoses[2]="";
            diagnoses[3]="";
            return diagnoses;
        }
        String[] userSchema = new String[1];
        userSchema[0]=schemaPrefixName;
        
        String[] filterFieldName = new String[2];
        Object[] filterFieldValue = new Object[2];
        String[] fieldsToReturn = new String[5];

        fieldsToReturn[0] = "sop_id";
        fieldsToReturn[1] = "sop_name";
        fieldsToReturn[2] = "status";
        fieldsToReturn[3] = "light";
        fieldsToReturn[4] = "expiration_date";
        filterFieldName[0]="user_id";
        filterFieldValue[0]=userInfoId;
        filterFieldName[1]=sopIdFieldName;
        filterFieldValue[1]=sopIdFieldValue;                
        Object[][] getUserProfileFieldValues = getUserProfileFieldValues(rdbm, filterFieldName, filterFieldValue, fieldsToReturn, userSchema);   
        if (getUserProfileFieldValues.length<=0){
            diagnoses[0]="ERROR";
            diagnoses[1]="The user "+userInfoId+" has no the sop "+sopIdFieldValue+ " assigned to.";
            return diagnoses;
        }
                
        if (getUserProfileFieldValues[0][3].toString().contains("GREEN")){
            
            if (certifyManagement && getUserProfileFieldValues[0][4]!=null){                
                Date now = new Date();
                if (now.after((Date) getUserProfileFieldValues[0][4])){
                    diagnoses[0]="SOP_CERTIF_EXPIRED";
                    diagnoses[1]="The user "+userInfoId+" was certified for the sop "+sopIdFieldValue+" but it expired on "+getUserProfileFieldValues[0][4].toString();
                    diagnoses[2]="xf06a@FontAwesome";
                    diagnoses[3]="The user "+userInfoId+" was certified for the sop "+sopIdFieldValue+" but it expired on "+getUserProfileFieldValues[0][4].toString();
                    return diagnoses;
                }
            }
            
            diagnoses[0]="PASS";  
            diagnoses[2]="xf046@FontAwesome"; 
            diagnoses[3]="The user "+userInfoId+" is currently certified for the sop "+sopIdFieldValue;
            //xf24e --> Certification near expire
            
            return diagnoses;
        }
        else{
            diagnoses[0]="NOTPASS";            
            diagnoses[1]="The user "; //+userInfoId+" has the sop "+replaceNull(getUserProfileFieldValues[0][1].toString())+ " assigned to which current status is "+replaceNull(getUserProfileFieldValues[0][2].toString())+" and the light is "+replaceNull(getUserProfileFieldValues[0][3].toString());
            diagnoses[2]="xf05e@FontAwesome"; 
            diagnoses[3]="The user "+userInfoId+" is currently NOT certified for the sop "+sopIdFieldValue;
            return diagnoses;
        }               
    }
       
    // This function cannot be replaced by a single query through the rdbm because it run the query through the many procedures
    //      the user is involved on if so ....
        
    public Object[][] getUserProfileFieldValues(Rdbms rdbm, String[] filterFieldName, Object[] filterFieldValue, String[] fieldsToReturn, String[] schemaPrefix){                
        String tableName = "user_sop";
        LabPLANETArray labArr = new LabPLANETArray();
        Object[] diagnoses = new Object[0];
        
        if (fieldsToReturn.length<=0){
            diagnoses = LabPLANETPlatform.trapErrorMessage(rdbm, "LABPLANET_FALSE", classVersion, "Rdbms_NotFilterSpecified", new Object[]{tableName, schemaPrefix});
            String[][] getUserProfileNEW = new String[1][2];
            getUserProfileNEW[0][0]="ERROR";
            getUserProfileNEW[0][1]="No fields specified for fieldsToReturn";
            return getUserProfileNEW;}
                    
        if ((filterFieldName==null) || (filterFieldValue==null) || (schemaPrefix==null)){
            String[][] getUserProfileNEW = new String[1][4];
            getUserProfileNEW[0][0]="ERROR";
            getUserProfileNEW[0][1]="filterFieldName and/or filterFieldValue and/or schemaPrefix are null and this is not expected";
            if (filterFieldName==null){getUserProfileNEW[0][2]="filterFieldName is null";}else{getUserProfileNEW[0][2]="filterFieldName="+Arrays.toString(filterFieldName);}
            if (filterFieldValue==null){getUserProfileNEW[0][3]="filterFieldValue is null";}else{getUserProfileNEW[0][3]="filterFieldValue="+Arrays.toString(filterFieldValue);}
            return getUserProfileNEW;}       
                
        String query = "";
        for(String sPref: schemaPrefix){                    
            query = query+"(select ";
            for(String fRet: fieldsToReturn){
                query = query+" "+fRet+","; 
            }
            query=query.substring(0, query.length()-1);
            if (sPref.contains("data")){query = query+" from \""+ sPref+"\"."+tableName+" where 1=1";}
            else{query = query+" from \""+ sPref+"-data\"."+tableName+" where 1=1";}
            for(String fFN: filterFieldName){
                query = query+" and "+fFN;
                if (!fFN.contains("null")){query=query+"= ?";}
            }
            query = query+") union ";
        }       
        query=query.substring(0, query.length()-6);
        
        Object[] filterFieldValueAllSchemas = new Object[filterFieldValue.length*schemaPrefix.length];
        Integer i=0;
        for(String sPref: schemaPrefix){
            for(Object fVal: filterFieldValue){
                filterFieldValueAllSchemas[i]=fVal;    
                i++;
            }
        }               
        try{
            ResultSet res = rdbm.prepRdQuery(query, filterFieldValueAllSchemas);         
            res.last();
            Integer numLines=res.getRow();
            Integer numColumns=fieldsToReturn.length;
            res.first();
            Object[][] getUserProfileNEW=new Object[numLines][numColumns];
            for (Integer inumLines=0;inumLines<numLines;inumLines++){
                for (Integer inumColumns=0;inumColumns<numColumns;inumColumns++){
                    getUserProfileNEW[inumLines][inumColumns]=res.getObject(inumColumns+1);
                }
                res.next();
            }
            return getUserProfileNEW;                
        }catch(SQLException ex){}
            Object[] diagErr =  LabPLANETPlatform.trapErrorMessage(rdbm, "LABPLANET_FALSE", classVersion, "Rdbms_NoRecordsFound", 
                    new Object[]{tableName, query, Arrays.toString(schemaPrefix)});
            return labArr.array1dTo2d(diagErr, diagErr.length);
    }
    
    Integer  _notRequireddbGetUserSopBySopId(Rdbms rdbm, String schemaName, String UserId, Integer sopId) {

        LabPLANETArray labArr = new LabPLANETArray();
        String schemaDataName = "data";
        schemaName = LabPLANETPlatform.buildSchemaName(schemaDataName, schemaName);
        
        String query = "";
        schemaName = "\""+schemaName+"\"";
        query = "select user_sop_id from " + schemaName + ".user_sop "        
            + "   where user_id =? and sop_id = ? ";
        try{     
            ResultSet res = rdbm.prepRdQuery(query, new Object [] {UserId, sopId.toString()});          
            res.last();            

            if (res.getRow()>0) return res.getInt("user_sop_id");

            return null;
        }catch (SQLException ex) {
            Logger.getLogger(query).log(Level.SEVERE, null, ex);
            return null;
        }
            
    }

    public Object[] addSopToUserById(Rdbms rdbm, String schemaName, String userInfoId, Integer sopId){
        return addSopToUserInternalLogic(rdbm, schemaName, userInfoId, "sop_id", sopId);
    }   
    public Object[] addSopToUserById(Rdbms rdbm, String schemaName, String userInfoId, String sopId){
        return addSopToUserInternalLogic(rdbm, schemaName, userInfoId, "sop_id", sopId);
    }   
    public Object[] addSopToUserByName(Rdbms rdbm, String schemaName, String userInfoId, String sopName){
        return addSopToUserInternalLogic(rdbm, schemaName, userInfoId, "sop_id", sopName);
    }    
    public Object[] addSopToUserInternalLogic(Rdbms rdbm, String schemaName, String userInfoId, String sopIdFieldName, Object sopIdFieldValue){
        LabPLANETArray labArr = new LabPLANETArray();
        String schemaDataName = "data";
        schemaName = LabPLANETPlatform.buildSchemaName(schemaName, schemaDataName);
        String diagnoses = "";
        Sop s = null;
        tableName = "user_sop";
        Object[] exists = rdbm.existsRecord(rdbm, schemaName, tableName, new String[]{"user_id", sopIdFieldName}, new Object[]{userInfoId, sopIdFieldValue});
                
        if ("LABPLANET_TRUE".equalsIgnoreCase(exists[0].toString())){
            String messageCode = "UserSop_sopAlreadyAssignToUser";
            Object[] errorDetailVariables = new Object[0] ;
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, sopIdFieldValue);          
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, userInfoId);          
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, schemaName);          
            return LabPLANETPlatform.trapErrorMessage(rdbm, "LABPLANET_FALSE", classVersion, diagnoses, javaDocValues);
        }
        
        Object[] diagnosis = rdbm.insertRecordInTable(rdbm, schemaName, "user_sop", new String[]{"user_id", sopIdFieldName}, new Object[]{userInfoId, sopIdFieldValue});
        if ("LABPLANET_FALSE".equalsIgnoreCase(diagnosis[0].toString())){
            return diagnosis;
        }else{
            String messageCode = "UserSop_sopAddedToUser";
            Object[] errorDetailVariables = new Object[0] ;
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, sopIdFieldValue);          
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, userInfoId);          
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, schemaName);          
            return LabPLANETPlatform.trapErrorMessage(rdbm, "LABPLANET_TRUE", classVersion, diagnoses, javaDocValues);
        }
    }
    
    public String[] _notRequiredgetUserSopFilter(String userInfoId){
        String[] theSops = null;        
        return theSops;
    }
    
}
