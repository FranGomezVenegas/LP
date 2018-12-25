/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.labplanet.servicios.app;

import LabPLANET.utilities.LabPLANETArray;
import LabPLANET.utilities.LabPLANETFrontEnd;
import databases.Rdbms;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import databases.Token;
import org.json.simple.JSONObject;

/**
 *
 * @author Administrator
 */
public class appHeaderAPI extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String language = "en";

        LabPLANETArray labArr = new LabPLANETArray();
        LabPLANETFrontEnd labFrEnd = new LabPLANETFrontEnd();

        Rdbms rdbm = new Rdbms();     
    
        try (PrintWriter out = response.getWriter()) {            
            String[] errObject = new String[]{"Servlet sampleAPI at " + request.getServletPath()};            
            
            String actionName = request.getParameter("actionName");
            String finalToken = request.getParameter("finalToken");
            if (actionName==null & finalToken==null) {
                errObject = labArr.addValueToArray1D(errObject, "Error Status Code: "+HttpServletResponse.SC_BAD_REQUEST);
                errObject = labArr.addValueToArray1D(errObject, "API Error Message: actionName and finalToken are mandatory params for this API");                    
                Object[] errMsg = labFrEnd.responseError(errObject, language, null);
                response.sendError((int) errMsg[0], (String) errMsg[1]);   
                return ;
            }                     
            switch (actionName.toUpperCase()){
                case "GETAPPHEADER":          
                    
                    String personFieldsName = request.getParameter("personFieldsName");
                    
                    Token token = new Token();
                    String[] tokenParams = token.tokenParamsList();
                    String[] tokenParamsValues = token.validateToken(finalToken, tokenParams);

                    String dbUserName = tokenParamsValues[labArr.valuePosicInArray(tokenParams, "userDB")];
                    String dbUserPassword = tokenParamsValues[labArr.valuePosicInArray(tokenParams, "userDBPassword")];
                    String internalUserID = tokenParamsValues[labArr.valuePosicInArray(tokenParams, "internalUserID")];         
                    String userRole = tokenParamsValues[labArr.valuePosicInArray(tokenParams, "userRole")];       
                    
/*                    JsonObject json = Json.createObjectBuilder()
                            .add("DBUser", dbUserName)
                            .add("userRole", userRole).build();
*/
                    JSONObject personInfoJsonObj = new JSONObject();

                    if ( personFieldsName!=null){
                        String[] personFieldsNameArr = personFieldsName.split("\\|");
                        
                        if (!rdbm.startRdbms(dbUserName, dbUserPassword)) {
                                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);           
                                        errObject = labArr.addValueToArray1D(errObject, "Error Status Code: "+"Connection not established");                        
                                        out.println(Arrays.toString(errObject));              
                                        return;                
                        }
                        Object[][] personInfoArr = rdbm.getRecordFieldsByFilter(rdbm, "config", "person", 
                             new String[]{"person_id"}, new String[]{internalUserID}, personFieldsNameArr);             
                        if ("LABPLANET_FALSE".equals(personInfoArr[0][0].toString())){                                                                                                                                                   
                            Object[] errMsg = labFrEnd.responseError(labArr.array2dTo1d(personInfoArr), language, null);
                            response.sendError((int) errMsg[0], (String) errMsg[1]);   
                            return;
                        }
                        for (int iFields=0; iFields<personFieldsNameArr.length; iFields++ ){
                            personInfoJsonObj.put(personFieldsNameArr[iFields], personInfoArr[0][iFields]);
                        }
                    }             
                    response.getWriter().write(personInfoJsonObj.toString());                                                                                                                           
                    Response.ok().build();                     
                    rdbm.closeRdbms();
                    return;
                default:      
                    errObject = labArr.addValueToArray1D(errObject, "Error Status Code: "+HttpServletResponse.SC_BAD_REQUEST);
                    errObject = labArr.addValueToArray1D(errObject, "API Error Message: actionName "+actionName+ " not recognized as an action by this API");                                        
                    Object[] errMsg = labFrEnd.responseError(errObject, language, null);
                    response.sendError((int) errMsg[0], (String) errMsg[1]);                   
                    rdbm.closeRdbms();
                    return;                           
            }            
        }catch(Exception e){            
            String exceptionMessage = e.getMessage();           
            Object[] errMsg = labFrEnd.responseError(new String[]{exceptionMessage}, language, null);
            response.sendError((int) errMsg[0], (String) errMsg[1]);              
            return;
        }                                       
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
