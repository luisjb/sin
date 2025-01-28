/* Cuora 
 * 
 * Copyright (c) 2014 Pablo Llanos <pllanos@ucod.com.ar>
 * United Coders - www.ucod.com.ar
 * All rights reserved.
 * 
 * Licensed under the United Coders Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.ucod.com.ar/licenses/ucpl-1.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package load.file;

import java.util.logging.Level;

/**
 * @author      Pablo Llanos
 * @version     %I%, %G%
 * @since       1.0
 */
public class NeoLogger implements com.ucod.util.Logger {

    private final java.util.logging.Logger logger;
    
    public NeoLogger(java.util.logging.Logger logger) {
        this.logger = logger;
    }
    
    @Override
    public void log(Level level, String msg, Object params[]) {
        logger.log(level, msg, params);
    }

    @Override
    public void log(Level level, String msg) {
        logger.log(level, msg);
    }

    @Override
    public void log(Level level, String msg, Throwable thrown) {
        logger.log(level, msg, thrown);
    }

}
