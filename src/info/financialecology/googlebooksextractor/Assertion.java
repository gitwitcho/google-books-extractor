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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple class to check the truth value of a statement and do one of two things:
 *    - stop execution if the level is 'ERR'
 *    - continue execution if the level id 'INFO'
 *    
 * TODO Add an assertion method that throws an exception 
 *
 * @author Gilbert Peffer
 *
 */
public class Assertion {
    
    private static final Logger logger = (Logger)LoggerFactory.getLogger(Assertion.class.getSimpleName());

    public enum Level {
        INFO,
        ERR;
    }
    
    public static Boolean assertStrict (Boolean a, Level level, String err) {
        if (a == false) {
            logger.error("Assertion failed: {}", err);

            if (level == Level.ERR) {
                logger.error("----- Stopping execution -----");
                System.exit(0);
            }
            else if (level == Level.INFO)
                return false;
        }
        
        return true;
    }

}
