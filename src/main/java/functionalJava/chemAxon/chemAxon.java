/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package functionalJava.chemAxon;

/**
 *
 * @author Administrator
 */
public class chemAxon {
    
    public void BasicExample(){
        Molecule molecule = MolImporter.importMol("C1=CC=CC=C1");
        //Standardizer standardizer = new Standardizer("aromatize");
        //standardizer.standardize(molecule);
    }
    
}
