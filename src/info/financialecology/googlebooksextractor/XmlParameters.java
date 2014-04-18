/*
 * Copyright (c) 2013 Gilbert Peffer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package info.financialecology.googlebooksextractor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.thoughtworks.xstream.XStream;

/**
 * TODO Add description
 * 
 * @author Gilbert Peffer
 *
 */
public abstract class XmlParameters {

    /**
     * Serialise an object to XML and write to the file {@code fileName}
     * 
     * @param fileName
     * @param params
     */
    protected static void writeParamsDefinition(String fileName, XmlParameters params) throws FileNotFoundException {
        XStream xstream = new XStream();
        try {
            FileOutputStream fs = new FileOutputStream(fileName);
            xstream.toXML(params, fs);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            throw e1;
        }
    }

    /**
     * Deserialising an object back from XML, which is provided by the file {@code fileName}
     * @param fileName
     * @param params
     */
    protected static XmlParameters readParams(String fileName, XmlParameters params) throws FileNotFoundException {
        XStream xstream = new XStream();
        try {
            FileInputStream fis = new FileInputStream(fileName);
            xstream.fromXML(fis, params);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            throw ex;
        }
        
        return params;
    }
    

    /**
     * 
     */
    public XmlParameters() {
        super();
    }

}