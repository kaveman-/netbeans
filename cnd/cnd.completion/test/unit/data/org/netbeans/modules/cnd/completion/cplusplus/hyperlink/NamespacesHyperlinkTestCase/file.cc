/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

#include "file.h"

namespace S1 {
    int var1;

    void foo() {
        // S1 content must be visible with and without S1:: prefix
        S1::foo();
        S1::var1 = 10;
        foo();
        var1 = 11;
        // S2 content must be visible with prefixes
        S1::S2::boo();
        S1::S2::var2 = 100;
        S2::boo();
        S2::var2 = 101;
    }

    namespace S2 {
        int var2;
        
        void boo() {
            // S1 content must be visible with and without S1:: prefix
            S1::foo();
            S1::var1 = 12;
            foo();
            var1 = 13;
            // S2 content must be visible with and without prefixes
            S1::S2::boo();
            S1::S2::var2 = 102;
            S2::boo();
            S2::var2 = 103;
            boo();
            var2 = 104;
        }
        
        void funS2() {
            clsS1 s1;
            s1.clsS1pubFun();            
            
            clsS2 s2;
            s2.clsS2pubFun();
        }
        
        void clsS2::clsS2pubFun() {
            
        }
    }
    
    void funS1() {
        clsS1 s1;
        s1.clsS1pubFun();
        
        S2::clsS2 s2;
        s2.clsS2pubFun();
    }
    
    void clsS1::clsS1pubFun() {
        
    }
    
    extern int myCout;
}

