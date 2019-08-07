/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LabPLANET.utilities;

import java.util.Arrays;

/**
 *
 * @author Administrator
 */
public class LPParadigm {
    private LPParadigm(){    throw new IllegalStateException("Utility class");}    
    
    public static Object[] fieldNameValueArrayChecker (String[] fName, Object[] fValue){
        Object[] diagnoses = null;
        String errorCode ="";
        Object[] errorDetailVariables= new Object[0];

        diagnoses = LPArray.checkTwoArraysSameLength(fName, fValue);
        if (!"LABPLANET_TRUE".equalsIgnoreCase(diagnoses[0].toString())){
           errorCode = "DataSample_FieldArraysDifferentSize";
           errorDetailVariables = LPArray.addValueToArray1D(errorDetailVariables, Arrays.toString(fName));
           errorDetailVariables = LPArray.addValueToArray1D(errorDetailVariables, Arrays.toString(fValue));
           return LPPlatform.trapErrorMessage("LABPLANET_FALSE", errorCode, errorDetailVariables);
        }
        
        if (LPArray.duplicates(fName)){
           errorCode = "DataSample_FieldsDuplicated";
           errorDetailVariables = LPArray.addValueToArray1D(errorDetailVariables, Arrays.toString(fName));
           return LPPlatform.trapErrorMessage("LABPLANET_FALSE", errorCode, errorDetailVariables);                      
        }        
        diagnoses = LPArray.addValueToArray1D(diagnoses, "LABPLANET_TRUE");
        return diagnoses;
                
    }
    
}
