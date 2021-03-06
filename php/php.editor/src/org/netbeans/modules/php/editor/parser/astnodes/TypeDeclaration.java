/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.editor.parser.astnodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents base class for class declaration and interface declaration
 */
public abstract class TypeDeclaration extends Statement {

    private Identifier name;
    private ArrayList<Expression> interfaces = new ArrayList<>();
    private Block body;

    public TypeDeclaration(int start, int end, final Identifier name, final Expression[] interfaces, final Block body) {
        super(start, end);

        if (name == null || body == null) {
            throw new IllegalArgumentException();
        }

        this.name = name;
        this.body = body;

        if (interfaces != null) {
            this.interfaces.addAll(Arrays.asList(interfaces));
        }
    }

    /**
     * The body component of this type declaration node
     * @return body component of this type declaration node
     */
    public Block getBody() {
        return body;
    }

    /**
     * The name of the type declaration node
     * @return name of the type declaration node
     */
    public Identifier getName() {
        return this.name;
    }

    /**
     * List of interfaces that this type implements / extends
     */
    public List<Expression> getInterfaes() {
        return this.interfaces;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Expression expression : getInterfaes()) {
            sb.append(expression).append(","); //NOI18N
        }
        return getName() + (sb.length() > 0 ? " " + sb.toString() : " ") + getBody(); //NOI18N
    }

}
