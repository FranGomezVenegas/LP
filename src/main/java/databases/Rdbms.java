/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package databases;

import com.sun.rowset.CachedRowSetImpl;
import LabPLANET.utilities.LabPLANETArray;
import LabPLANET.utilities.LabPLANETPlatform;
import java.io.FileWriter;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;


/**
 *
 * @author Administrator
 */
public class Rdbms {

    String classVersion = "0.1";
    String errorCode = "";
    String[] errorDetailVariables = new String[0];
//    LabPLANETArray labArr = new LabPLANETArray();
//    LabPLANETPlatform labPlat = new LabPLANETPlatform();
    String[] javaDocFields = new String[0];
    Object[] javaDocValues = new Object[0];
    String javaDocLineName = "";

    String schemaDataName = "data";
    String schemaConfigName = "config";
    String tableName = "user_method"; 
    
    String[] diagnoses = new String[7];    

    private Connection conn = null;
    private Boolean isStarted = false;
    private Integer timeout;
    private String lastError = "";

    public Rdbms() {
        //default query timeout
        this.timeout = 5;        
    }    

    public Boolean startRdbms(Rdbms rdbm, String user, String pass) throws SQLException{
        try {
            ResourceBundle prop = ResourceBundle.getBundle("parameter.config.config");
            String datasrc = prop.getString("datasource");            
            Context ctx = new InitialContext();
            DataSource ds = (DataSource)ctx.lookup(datasrc);
            ds.setLoginTimeout(getTimeout());
            Connection connection = ds.getConnection(user, pass);
            return connection != null;
                
        } catch (NamingException ex) {
            Logger.getLogger(Rdbms.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public Boolean startRdbms(String user, String pass) throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException, NamingException{
        ResourceBundle prop = ResourceBundle.getBundle("parameter.config.config");
        String datasrc = prop.getString("datasource");
        Integer to = Integer.valueOf(prop.getString("dbtimeout"));
        setTimeout(to);
                
              Context ctx = new InitialContext();
              DataSource ds = (DataSource)ctx.lookup(datasrc);
          
              ds.setLoginTimeout(getTimeout());
              setConnection(ds.getConnection(user, pass));
              
//              String url = prop.getString("dburl");
//              Properties props = new Properties();
//              
//                props.setProperty("user",user);
//                props.setProperty("password",pass);
//                props.setProperty("ssl","true");
//                Connection conn = DriverManager.getConnection(url, props);
              
          if(getConnection()!=null){
            setIsStarted(Boolean.TRUE);
            return true;
          }else{
            setIsStarted(Boolean.FALSE);
            return false;
          }  
    //return getIsStarted();
    }

    public void closeRdbms() throws SQLException{
        if(getConnection()!=null){
            conn.close();
            setIsStarted(Boolean.FALSE);
            }
    }  
    
    public Integer getTimeout() { return timeout;}

    public void setTimeout(Integer timeout) { this.timeout = timeout;}

    private void setLastError(String txterror){ lastError = txterror;}
    
    public String getLastError(){return lastError;}

    private void setConnection(Connection con){ conn=con; }
    
    public Connection getConnection(){ return conn; }

    public Boolean getIsStarted() { return isStarted;}
    
    private void setIsStarted(Boolean isStart) { this.isStarted = isStart;}
    
    public String buildSqlStatement (String operation, String schemaName, String tableName, String[] whereFieldNames, String[] whereOperation, Object[] whereFieldValues, String[] fieldsToRetrieve, String[] fieldsToOrder, String[] fieldsToGroup){
        LabPLANETArray labArr = new LabPLANETArray();
        String query = "";
        switch (operation.toUpperCase()){            
            case "SELECT":
                Integer i=1;
                Boolean containsInClause = false;
                Object[] whereFieldValuesNew = new Object[0];

                String fieldsToRetrieveStr = "";
                for (String fn: fieldsToRetrieve){fieldsToRetrieveStr = fieldsToRetrieveStr + fn + ", ";}
                fieldsToRetrieveStr = fieldsToRetrieveStr.substring(0, fieldsToRetrieveStr.length()-2);
                query = "select " + fieldsToRetrieveStr + " from " + schemaName + "." + tableName
                        + "   where " ;

                for (String fn: whereFieldNames){
                        if ( i >1){query = query + " and ";}

                        if ( fn.toUpperCase().contains("NULL")){ query = query + fn;}
                        else if (fn.toUpperCase().contains(" LIKE")){ query = query + fn + " ? ";} 
                        else if (fn.toUpperCase().contains(" IN")){ 
                            Integer posicINClause = fn.toUpperCase().indexOf("IN");
                            String separator = fn;
                            separator = separator.substring(posicINClause+2, posicINClause+3);
                            separator = separator.trim();
                            separator = separator.replace(" IN", "");  
                            containsInClause = true;
                            String textSpecs = (String) whereFieldValues[i-1];
                            String[] textSpecArray = textSpecs.split("\\"+separator);
                            query = query + fn.replace(separator, "") + "(" ;
                            for (Integer iNew=0;iNew<i-1;iNew++){
                                whereFieldValuesNew[iNew] = whereFieldValues[i];                        
                            }
                            for (String f: textSpecArray){
                                query = query + "?,";
                                whereFieldValuesNew = labArr.addValueToArray1D(whereFieldValuesNew, textSpecArray);                        
                                i++;
                            }
                            for (Integer j=i;j<=whereFieldValues.length;j++){
                                whereFieldValuesNew = labArr.addValueToArray1D(whereFieldValuesNew, whereFieldValues[j]);  
                            }
                            query = query.substring(0, query.length()-1);
                            query = query + ")" ;
                            whereFieldValues = whereFieldValuesNew;
                        }                 
                        else {query = query + fn + "=? ";}

                        i++;
                    }    
        
//                String query = "";
//                query = "select " + fieldsToRetrieve[0] + " from " + schemaName + "." + tableName
//                        + "   where " + whereFieldName + "=? ";                
                return query;
                //break;
            default:
                break;
        }
        
        return query;
    }
    
    public Object[] existsRecord(Rdbms rdbm, String schemaName, String tableName, String[] keyFieldName, Object keyFieldValue){
        
        String[] diagnoses = new String[6];
        LabPLANETArray labArr = new LabPLANETArray();        
        LabPLANETPlatform labPlat = new LabPLANETPlatform();        
        
        String query = buildSqlStatement("SELECT", schemaName, tableName,
                keyFieldName, new String[]{"="}, null, keyFieldName,  null, null);          
        try{
            ResultSet res;
            res = rdbm.prepRdQuery(query, new Object[] {keyFieldValue});
            res.last();

            if (res.getRow()>0){
                errorCode = "Rbdms_existsRecord_RecordFound";
                errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, keyFieldValue.toString());
                errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, tableName);
                errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, schemaName);
                return (String[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_TRUE", classVersion, errorCode, errorDetailVariables);                
            }else{
                errorCode = "Rbdms_existsRecord_RecordNotFound";
                errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, keyFieldValue.toString());
                errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, tableName);
                errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, schemaName);
                return (String[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_FALSE", classVersion, errorCode, errorDetailVariables);                
            }
        }catch (SQLException er) {
            String ermessage=er.getLocalizedMessage()+er.getCause();
            Logger.getLogger(query).log(Level.SEVERE, null, er);     
            errorCode = "Rdbms_dtSQLException";
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, ermessage);
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, query);
            return (Object[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_FALSE", classVersion, errorCode, errorDetailVariables);                                       
        }                    
    }

    public Object[] existsRecord(Rdbms rdbm, String schemaName, String tableName, String[] keyFieldNames, Object[] keyFieldValues){
        
        String[] diagnoses = new String[6];
        LabPLANETArray labArr = new LabPLANETArray();
       
        LabPLANETPlatform labPlat = new LabPLANETPlatform();   
        
        Object[] filteredValues = new Object[0];
        
        if (keyFieldNames.length==0){
           errorCode = "Rdbms_NotFilterSpecified";
           errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, tableName);
           errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, schemaName);          
           return (Object[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_FALSE", classVersion, errorCode, errorDetailVariables);                         
        }
        
        String query = "";
        query = "select " + keyFieldNames[0] + " from " + schemaName + "." + tableName
                + "   where " ;
        
        for (Integer iFv=0;iFv<keyFieldValues.length;iFv++){
            if (iFv>0){query = query + " and ";}
            query = query + keyFieldNames[iFv];

            Boolean addToFilter = true;
            if (keyFieldValues[iFv].toString().equalsIgnoreCase("IN()")){addToFilter=false;}
            if (keyFieldValues[iFv].toString().equalsIgnoreCase("IS NULL")){addToFilter=false;}
            if (keyFieldValues[iFv].toString().equalsIgnoreCase("IS NOT NULL")){addToFilter=false;}
            if (addToFilter){
                filteredValues = labArr.addValueToArray1D(filteredValues, keyFieldValues[iFv]);
                query = query + "=?";
            }
        }        
        try{
            ResultSet res = rdbm.prepRdQuery(query, filteredValues);
            res.last();

            if (res.getRow()>0){
                errorCode = "Rbdms_existsRecord_RecordFound";
                errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, Arrays.toString(filteredValues));
                errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, tableName);
                errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, schemaName);
                return (String[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_TRUE", classVersion, errorCode, errorDetailVariables);                
            }else{
                errorCode = "Rbdms_existsRecord_RecordNotFound";
                errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, Arrays.toString(filteredValues));
                errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, tableName);
                errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, schemaName);
                return (String[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_FALSE", classVersion, errorCode, errorDetailVariables);                
            }
        }catch (SQLException er) {
            String ermessage=er.getLocalizedMessage()+er.getCause();
            Logger.getLogger(query).log(Level.SEVERE, null, er);     
            errorCode = "Rdbms_dtSQLException";
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, ermessage);
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, query);
            return (Object[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_FALSE", classVersion, errorCode, errorDetailVariables);                         
        }                    
    }

    public Object[][] getRecordFieldsByFilter(Rdbms rdbm, String schemaName, String tableName, String[] whereFieldNames, Object[] whereFieldValues, String[] fieldsToRetrieve){
        
        Object[][] diagnoses = new Object[1][6];                
        LabPLANETArray labArr = new LabPLANETArray();       
        LabPLANETPlatform labPlat = new LabPLANETPlatform();   
        
        schemaName = labPlat.buildSchemaName(schemaName, "");
        
        if (whereFieldNames.length==0){
           errorCode = "Rdbms_NotFilterSpecified";
           errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, tableName);
           errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, schemaName);          
           Object[] diagnosesError = (Object[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_FALSE", classVersion, errorCode, errorDetailVariables);                         
           return labArr.array1dTo2d(diagnosesError, 6);
        }
        String query = buildSqlStatement("SELECT", schemaName, tableName,
                whereFieldNames, null, whereFieldValues,
                fieldsToRetrieve, null, null);        
            try{
                Boolean containsInClause = false;
                ResultSet res = null;
                if ( containsInClause ){
                    res = rdbm.prepRdQuery(query, whereFieldValues);
                    res.last();
                }else{
                    res = rdbm.prepRdQuery(query, whereFieldValues);
                    res.last();
                }
            if (res.getRow()>0){
             Integer totalLines = res.getRow();
             res.first();
             Integer icurrLine = 0;   
             
             Object[][] diagnoses2 = new Object[totalLines][fieldsToRetrieve.length];
             while(icurrLine<=totalLines-1) {
                //fieldValues = labArr.encryptTableFieldArray(schemaName, tableName, fieldNames, fieldValues);                 
                for (Integer icurrCol=0;icurrCol<fieldsToRetrieve.length;icurrCol++){
                    Object currValue = res.getObject(icurrCol+1);
                    diagnoses2[icurrLine][icurrCol] =  currValue;
                }        
                res.next();
                icurrLine++;
             }         
             //diagnoses2 = labArr.decryptTableFieldArray(schemaName, tableName, fieldsToRetrieve, (Object[][]) diagnoses2); 
//                diagnoses2 = labArr.decryptTableFieldArray(schemaName, tableName, fieldsToRetrieve, diagnoses2);
                return diagnoses2;
            }else{
                errorCode = "Rdbms_NoRecordsFound";
                errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, tableName);
                errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, Arrays.toString(whereFieldValues) );
                errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, schemaName);
                Object[] diagnosesError = (Object[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_FALSE", classVersion, errorCode, errorDetailVariables);                         
                return labArr.array1dTo2d(diagnosesError, 6);
            }
        }catch (SQLException er) {
            String ermessage=er.getLocalizedMessage()+er.getCause();
            Logger.getLogger(query).log(Level.SEVERE, null, er);     
            errorCode = "Rdbms_dtSQLException";
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, ermessage);
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, query);
            Object[] diagnosesError = (Object[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_FALSE", classVersion, errorCode, errorDetailVariables);                         
            return labArr.array1dTo2d(diagnosesError, 6);
        }                    
    }

    public Object[][] getRecordFieldsByFilter(Rdbms rdbm, String schemaName, String[] tableName, String[] whereFieldNames, Object[] whereFieldValues, String[] fieldsToRetrieve){
        
        Object[][] diagnoses = new Object[1][6];        
        LabPLANETArray labArr = new LabPLANETArray();       
        LabPLANETPlatform labPlat = new LabPLANETPlatform();   
        
        if (whereFieldNames.length==0){
           errorCode = "Rdbms_NotFilterSpecified";
           errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, tableName);
           errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, schemaName);          
           Object[] diagnosesError = (Object[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_FALSE", classVersion, errorCode, errorDetailVariables);                         
           return labArr.array1dTo2d(diagnosesError, 6);               
        }
        
        String query = "";
        String fieldsToRetrieveStr = "";
        for (String fn: fieldsToRetrieve){fieldsToRetrieveStr = fieldsToRetrieveStr + fn + ", ";}
        fieldsToRetrieveStr = fieldsToRetrieveStr.substring(0, fieldsToRetrieveStr.length()-2);
        query = "select " + fieldsToRetrieveStr + " from ";
        Integer i=1;
        for (String tbl: tableName){
            if (i>1){query = query + " , ";}
            query = query + " " + schemaName + "." + tbl;
            i++;
        }    
        query = query + "   where " ;
        i=1;
        for (String fn: whereFieldNames){
                if (i>1){query = query + " and ";}
                
                if ( (fn.toUpperCase().contains("NULL")) || (fn.toUpperCase().contains("LIKE")) ){
                    query = query + fn;
                }else {query = query + fn + "=? ";}
                
                i++;
        }        
        try{
            ResultSet res = rdbm.prepRdQuery(query, whereFieldValues);
            res.last();

            if (res.getRow()>0){
             Integer totalLines = res.getRow();
             res.first();
             Integer icurrLine = 0;   
             
             Object[][] diagnoses2 = new Object[totalLines][fieldsToRetrieve.length];
             while(icurrLine<=totalLines-1) {
                for (Integer icurrCol=0;icurrCol<fieldsToRetrieve.length;icurrCol++){
                    Object currValue = res.getObject(icurrCol+1);
                    diagnoses2[icurrLine][icurrCol] =  currValue;
                }        
                res.next();
                icurrLine++;
             }
                diagnoses2 = labArr.decryptTableFieldArray(schemaName, tableName[0], fieldsToRetrieve, diagnoses2);
                return diagnoses2;
            }else{
                errorCode = "Rbdms_existsRecord_RecordNotFound";
                errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, Arrays.toString(whereFieldValues));
                errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, tableName);
                errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, schemaName);
                Object[] diagnosesError = (Object[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_FALSE", classVersion, errorCode, errorDetailVariables);                         
                return labArr.array1dTo2d(diagnosesError, 6);               
            }
        }catch (SQLException er) {
            String ermessage=er.getLocalizedMessage()+er.getCause();
            Logger.getLogger(query).log(Level.SEVERE, null, er);     
            errorCode = "Rdbms_dtSQLException";
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, ermessage);
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, query);
            Object[] diagnosesError = (Object[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_FALSE", classVersion, errorCode, errorDetailVariables);                         
            return labArr.array1dTo2d(diagnosesError, 6);               
        }                    
    }

    public Object[][] getRecordFieldsByFilter(Rdbms rdbm, String schemaName, String tableName, String[] whereFieldNames, Object[] whereFieldValues, String[] fieldsToRetrieve, String[] orderBy){
        
        Object[][] diagnoses = new Object[1][6];        
        LabPLANETArray labArr = new LabPLANETArray();       
        LabPLANETPlatform labPlat = new LabPLANETPlatform();   
        
        if (whereFieldNames.length==0){
           errorCode = "Rdbms_NotFilterSpecified";
           errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, tableName);
           errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, schemaName);          
           Object[] diagnosesError = (Object[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_FALSE", classVersion, errorCode, errorDetailVariables);                         
           return labArr.array1dTo2d(diagnosesError, 6);               
        }
        
        String query = "";
        String fieldsToRetrieveStr = "";
        for (String fn: fieldsToRetrieve){fieldsToRetrieveStr = fieldsToRetrieveStr + fn + ", ";}
        fieldsToRetrieveStr = fieldsToRetrieveStr.substring(0, fieldsToRetrieveStr.length()-2);
        query = "select " + fieldsToRetrieveStr + " from " + schemaName + "." + tableName
                + "   where " ;
        Integer i=1;
        for (String fn: whereFieldNames){
                if (i>1){query = query + " and ";}
                
                if ( (fn.toUpperCase().contains("NULL")) || (fn.toUpperCase().contains("LIKE")) ){
                    query = query + fn;
                }else {query = query + fn + "=? ";}
                
                i++;
        }    
        i=1;
        query = query + " order by ";
        for (String sortFld: orderBy){
                if (i>1){query = query + ", ";}                
                query = query + sortFld;               
                i++;
        }         
        try{
            ResultSet res = rdbm.prepRdQuery(query, whereFieldValues);
            res.last();

            if (res.getRow()>0){
             Integer totalLines = res.getRow();
             res.first();
             Integer icurrLine = 0;   
             
             Object[][] diagnoses2 = new Object[totalLines][fieldsToRetrieve.length];
             while(icurrLine<=totalLines-1) {
                for (Integer icurrCol=0;icurrCol<fieldsToRetrieve.length;icurrCol++){
                    Object currValue = res.getObject(icurrCol+1);
                    diagnoses2[icurrLine][icurrCol] =  currValue;
                }        
                res.next();
                icurrLine++;
             }
                diagnoses2 = labArr.decryptTableFieldArray(schemaName, tableName, fieldsToRetrieve, diagnoses2);
                return diagnoses2;
            }else{
                errorCode = "Rdbms_NoRecordsFound";
                errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, tableName);
                errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, Arrays.toString(whereFieldValues) );
                errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, schemaName);
                Object[] diagnosesError = (Object[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_FALSE", classVersion, errorCode, errorDetailVariables);                         
                return labArr.array1dTo2d(diagnosesError, 6);                
            }
        }catch (SQLException er) {
            String ermessage=er.getLocalizedMessage()+er.getCause();
            Logger.getLogger(query).log(Level.SEVERE, null, er);     
            errorCode = "Rdbms_dtSQLException";
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, ermessage);
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, query);
            Object[] diagnosesError = (Object[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_FALSE", classVersion, errorCode, errorDetailVariables);                         
            return labArr.array1dTo2d(diagnosesError, 6);             
        }                    
    }

    public Object[] insertRecordInTable(Rdbms rdbm, String schemaName, String tableName, String[] fieldNames, Object[] fieldValues){
        // fieldValues = labArr.encryptTableFieldArray(schemaName, tableName, fieldNames, fieldValues);
        String[] diagnoses = new String[7];
        LabPLANETArray labArr = new LabPLANETArray();       
        LabPLANETPlatform labPlat = new LabPLANETPlatform();
        LabPLANETArray labArray = new LabPLANETArray();

        if (fieldNames.length==0){
           errorCode = "Rdbms_NotFilterSpecified";
           errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, tableName);
           errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, schemaName);          
           return (Object[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_FALSE", classVersion, errorCode, errorDetailVariables);                         
        }
        if (fieldNames.length!=fieldValues.length){
           errorCode = "DataSample_FieldArraysDifferentSize";
           errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, Arrays.toString(fieldNames));
           errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, Arrays.toString(fieldValues));
           return (String[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_FALSE", classVersion, errorCode, errorDetailVariables);
        }
        String query = "";
        String fieldNamesStr = "";
        for (String fn: fieldNames){fieldNamesStr = fieldNamesStr + fn + ", ";}
        fieldNamesStr = fieldNamesStr.substring(0, fieldNamesStr.length()-2);
        query = "Insert into " + schemaName + "." + tableName + " (" + fieldNamesStr + ") values ( " ;
        Integer i=1;
        for (String fn: fieldNames){
            if (i==1){query = query + "? ";i++;}
            else{query = query + ", ? ";}
            i++;
        }
        query = query + ") ";
        try {                        
            fieldValues = labArr.encryptTableFieldArray(schemaName, tableName, fieldNames, (Object[]) fieldValues); 
            int numr = rdbm.prepUpQueryK(query, fieldValues, 1);
            fieldValues = labArr.decryptTableFieldArray(schemaName, tableName, fieldNames, (Object[]) fieldValues); 
            //ResultSet res = rdbm.prepRdQuery(query, fieldValues);
            //res.last();
            //if (numr>0){
            errorCode = "Rdbms_RecordCreated";
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, String.valueOf(numr));
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, query);
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, Arrays.toString(fieldValues));            
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, schemaName);                
            return (Object[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_TRUE", classVersion, errorCode, errorDetailVariables);                         
        } catch (SQLException er) {
            String ermessage=er.getLocalizedMessage()+er.getCause();
            Logger.getLogger(query).log(Level.SEVERE, null, er);     
            errorCode = "Rdbms_dtSQLException";
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, ermessage);
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, query);
            return (Object[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_FALSE", classVersion, errorCode, errorDetailVariables);                         
        }
    }
    
    public Object[] updateRecordFieldsByFilter(Rdbms rdbm, String schemaName, String tableName, String[] updateFieldNames, Object[] updateFieldValues, String[] whereFieldNames, Object[] whereFieldValues) throws SQLException{
        
        String[] diagnoses = new String[6];        
        LabPLANETArray labArr = new LabPLANETArray();       
        LabPLANETPlatform labPlat = new LabPLANETPlatform();  
        updateFieldValues = labArr.decryptTableFieldArray(schemaName, tableName, updateFieldNames, (Object[]) updateFieldValues);        
        
        if (whereFieldNames.length==0){
           errorCode = "Rdbms_NotFilterSpecified";
           errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, tableName);
           errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, schemaName);          
           return (Object[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_FALSE", classVersion, errorCode, errorDetailVariables);                         
        }
        
        String query = "";
        String updateFieldNamesStr = " set ";
        for (String fn: updateFieldNames){updateFieldNamesStr = updateFieldNamesStr + fn + "=?, ";}
        updateFieldNamesStr = updateFieldNamesStr.substring(0, updateFieldNamesStr.length()-2);
        query = "update " + schemaName + "." + tableName + updateFieldNamesStr
                + "   where " ;
        Integer i=1;
        for (String fn: whereFieldNames){
                String comparator = "=";
                if (fn.contains("<>")){comparator="<>"; fn=fn.replace("<>", "");}
                if (i==1){query = query + fn + comparator+"? ";i++;}
                else{query = query + " and "+ fn + comparator+"? ";}
        }       i++;
        for (Object fn: whereFieldValues){
            updateFieldValues = labArr.addValueToArray1D(updateFieldValues, fn);}

        updateFieldValues = labArr.encryptTableFieldArray(schemaName, tableName, updateFieldNames, (Object[]) updateFieldValues); 
        
        Integer numr = rdbm.prepUpQuery(query, updateFieldValues);
        if (numr>0){     
            errorCode = "Rdbms_RecordUpdated";
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, tableName);
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, Arrays.toString(whereFieldValues));
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, schemaName);     
            return (Object[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_TRUE", classVersion, errorCode, errorDetailVariables);   
        }else if(numr==-999){
            errorCode = "Rdbms_dtSQLException";
            String ermessage="The database cannot perform this sql statement: Schema: "+schemaName+". Table: "+tableName+". Query: "+query+", By the filter "+ Arrays.toString(whereFieldValues);
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, ermessage);
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, query);
            return (Object[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_FALSE", classVersion, errorCode, errorDetailVariables);   
        }else{   
            errorCode = "Rdbms_NoRecordsFound";
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, tableName);
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, Arrays.toString(whereFieldValues) );
            errorDetailVariables = labArr.addValueToArray1D(errorDetailVariables, schemaName);
            return (Object[]) labPlat.trapErrorMessage(rdbm, "LABPLANET_FALSE", classVersion, errorCode, errorDetailVariables);                         
        }
    }    

    public CachedRowSetImpl prepRdQuery(String consultaconinterrogaciones, Object [] valoresinterrogaciones) throws SQLException{
    //prepare statement para evitar sql injection
    LabPLANETArray labArr = new LabPLANETArray();        
    Object[] filteredValoresConInterrogaciones = new Object[0];     
     
     PreparedStatement prep=getConnection().prepareStatement(consultaconinterrogaciones);
        prep.setQueryTimeout(getTimeout());
        
        try{
            for (Integer i=0;i<valoresinterrogaciones.length;i++){
                Boolean addToFilter = true;
                if (valoresinterrogaciones[i].toString().equalsIgnoreCase("IN()")){addToFilter=false;}
                if (valoresinterrogaciones[i].toString().equalsIgnoreCase("IS NULL")){addToFilter=false;}
                if (valoresinterrogaciones[i].toString().equalsIgnoreCase("IS NOT NULL")){addToFilter=false;}
                if (addToFilter){
                    filteredValoresConInterrogaciones = labArr.addValueToArray1D(filteredValoresConInterrogaciones, valoresinterrogaciones[i]);}
            }
                
            buildPreparedStatement(filteredValoresConInterrogaciones, prep, null); 
        }catch(SQLException er){//cuando se envia un array 

        }
        
    ResultSet res = prep.executeQuery();
    CachedRowSetImpl crs = new CachedRowSetImpl();
    crs.populate(res);
    
    return crs; 
    }
    

    public Integer prepUpQuery(String consultaconinterrogaciones, Object [] valoresinterrogaciones) throws SQLException{
        Integer reg =  prepUpQuery(consultaconinterrogaciones, valoresinterrogaciones, null);    
        return reg;
    }
    
    public Integer prepUpQuery(String consultaconinterrogaciones, Object [] valoresinterrogaciones, Integer [] fieldtypes) throws SQLException{
        PreparedStatement prep=getConnection().prepareStatement(consultaconinterrogaciones);

        setTimeout(getTimeout());

        if (valoresinterrogaciones != null){
            buildPreparedStatement(valoresinterrogaciones, prep, fieldtypes); 
        }
        try{
            Integer res=prep.executeUpdate();
            return res; 
        }catch (SQLException er){
            return -999;
        }
    }
    
    public Integer prepUpQueryK(String consultaconinterrogaciones, Object [] valoresinterrogaciones, Integer indexposition) throws SQLException{
        Integer pk = 0;
        PreparedStatement prep=getConnection().prepareStatement(consultaconinterrogaciones, Statement.RETURN_GENERATED_KEYS);

        setTimeout(getTimeout());

        buildPreparedStatement(valoresinterrogaciones, prep, null); 
        Integer res = prep.executeUpdate();

        ResultSet rs = prep.getGeneratedKeys();

        if (rs.next()) {
          int newId = rs.getInt(indexposition);
          pk = newId;
        }

        return pk; 
    }
    
    public String [] getTableFieldsArrayEj(String schema, String table) throws SQLException{
        String sq = "select array(SELECT column_name || ''  FROM information_schema.columns WHERE table_schema = ? AND table_name   = ?) fields";
        CachedRowSetImpl res = prepRdQuery(sq, new Object[]{schema, table});
        
        String [] items = res.next() ? LabPLANETArray.getStringArray(res.getArray("fields").getArray()) : null ;
        
        return items;
    }
    
    public String getTableFieldsArrayEj(String schema, String table, String separator, Boolean addTableName) throws SQLException{
        String sq = "select array(SELECT column_name || ''  FROM information_schema.columns WHERE table_schema = ? AND table_name   = ?) fields";
        CachedRowSetImpl res = prepRdQuery(sq, new Object[]{schema, table});
        
        String [] items = res.next() ? LabPLANETArray.getStringArray(res.getArray("fields").getArray()) : null ;
        
        String tableFields = "";
        
        for (String f: items){
            if (tableFields.length()>0){tableFields=tableFields+separator;}
            if (addTableName){tableFields = tableFields+table+"."+f;            
            }else{tableFields = tableFields+f;}
        }
        return tableFields;
    }
        
    private void buildPreparedStatement(Object [] valoStrings, PreparedStatement prepsta, Integer [] fieldtypes) throws SQLException{
        Integer numfields = valoStrings.length;
        Integer indexval = 1;

        for(Integer numi=0;numi<numfields;numi++){
             Object obj = valoStrings[numi];

             String clase;

             if (obj != null){
             clase = obj.getClass().toString();
             }else{
             clase = "null";    
             }

               switch(clase){
               case "class java.lang.Integer":
               prepsta.setInt(indexval, (Integer)obj);
               break;
               case "class java.lang.Boolean":
               prepsta.setBoolean(indexval, (Boolean)obj);
               break;
               case "class java.sql.Date":
               prepsta.setDate(indexval, (java.sql.Date) obj);
               break;
               case "class java.util.Date":
                   Date dt = (Date) obj;
                   java.sql.Date sqlDate = new java.sql.Date(dt.getTime());                   
                   prepsta.setDate(indexval, (java.sql.Date) sqlDate);
               break;
               case "null":
               prepsta.setNull(indexval, fieldtypes[numi]);
               break; 
               case "class json.Na"://to skip fields
               break;  
               case "class [Ljava.lang.String;":
               Array array = conn.createArrayOf("VARCHAR", (Object []) obj);
               prepsta.setArray(indexval, array);
               break;
               default:
               prepsta.setString(indexval, (String) obj);
               break; 
           }
           
           if (!clase.equals("class json.Na")){
               indexval++;
           }
        }     
    }

    public ResourceBundle getParameterBundle(String configFile){
        ResourceBundle prop = ResourceBundle.getBundle("parameter.config."+configFile);
        return prop;
    }
    
    public String getParameterBundle(String configFile, String parameterName, String language){
        
        FileWriter fw = null;
        String newEntry = "";
        
        ResourceBundle prop = ResourceBundle.getBundle("parameter.config."+configFile+"_"+language); 
        if (!prop.containsKey(parameterName)){  
            return "";
        }else{    
            String paramValue = prop.getString(parameterName);
            return paramValue;
        }    
    }        

    public String getParameterBundle(String configFile, String parameterName){
        
        FileWriter fw = null;
        String newEntry = "";
        try {
            ResourceBundle prop = ResourceBundle.getBundle("parameter.config."+configFile); 
            if (!prop.containsKey(parameterName)){  
                return "";
            }else{    
                String paramValue = prop.getString(parameterName);
                return paramValue;
            }
        }catch (Exception e){
            
            return "";
        }    
    }        

    /**
     *
     * @param packageName
     * @param configFile
     * @param parameterName
     * @return
     */
    public String getParameterBundle(String packageName, String configFile, String parameterName, String language){
        ResourceBundle prop = null;
        FileWriter fw = null;
        String newEntry = "";
        try {
            if (language==null){
                prop = ResourceBundle.getBundle("parameter."+packageName+"."+configFile);                 
            }else{
                prop = ResourceBundle.getBundle("parameter."+packageName+"."+configFile+"_"+language); 
            }    
            if (!prop.containsKey(parameterName)){  
                return "";
            }else{    
                String paramValue = prop.getString(parameterName);
                return paramValue;
            }
        }catch (Exception e){
            
            return "";
        }    
    }        
    
    public Date getLocalDate(){
        Date de = new java.sql.Date(System.currentTimeMillis());        
        return de;}

    public Date getCurrentDate(){        
        Date de = new java.sql.Date(System.currentTimeMillis());        
        return de;}    
    
    public CachedRowSetImpl readQuery(String consulta) throws SQLException{
    Statement sta=getConnection().createStatement();
    sta.setQueryTimeout(getTimeout());
    
    ResultSet res=null;
            
        if(!"".equals(consulta)){
        res = sta.executeQuery(consulta);    
        }
        
    CachedRowSetImpl crs = new CachedRowSetImpl();
    crs.populate(res);
    
    return crs;    
    }
    
}
