/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.codeInspection.streamToLoop;

import com.intellij.codeInspection.util.OptionalUtil;
import com.intellij.psi.util.TypeConversionUtil;

/**
 * An interface representing the conditional expression to be generated in the resulting code
 *
 * @author Tagir Valeev
 */
interface Condition {
  String getType();

  String getCondition();

  String getTrueBranch();

  String getFalseBranch();

  default String asExpression() {
    return getCondition() + "?" + getTrueBranch() + ":" + getFalseBranch();
  }

  class Plain implements Condition {
    private final String myType;
    private final String myCondition;
    private final String myTrueBranch;
    private final String myFalseBranch;

    public Plain(String type, String condition, String trueBranch, String falseBranch) {
      myType = type;
      myCondition = condition;
      myTrueBranch = trueBranch;
      myFalseBranch = falseBranch;
    }

    public String getType() {
      return myType;
    }

    public String getCondition() {
      return myCondition;
    }

    public String getTrueBranch() {
      return myTrueBranch;
    }

    public String getFalseBranch() {
      return myFalseBranch;
    }
  }

  class Boolean implements Condition {
    private String myCondition;
    private boolean myInvert;

    public Boolean(String condition, boolean invert) {
      myCondition = condition;
      myInvert = invert;
    }

    @Override
    public String getType() {
      return "boolean";
    }

    @Override
    public String getCondition() {
      return myCondition;
    }

    @Override
    public String getTrueBranch() {
      return String.valueOf(!myInvert);
    }

    @Override
    public String getFalseBranch() {
      return String.valueOf(myInvert);
    }

    public Boolean negate() {
      return new Boolean(myCondition, !myInvert);
    }

    public Plain toPlain(String type, String trueBranch, String falseBranch) {
      return myInvert ? new Plain(type, myCondition, falseBranch, trueBranch) :
             new Plain(type, myCondition, trueBranch, falseBranch);
    }

    @Override
    public String asExpression() {
      return myInvert ? myCondition : "!("+myCondition+")";
    }
  }

  class Optional implements Condition {
    private final String myType;
    private final String myCondition;
    private final String myPresentExpression;
    private final String myTypeArgument;

    Optional(String type, String condition, String presentExpression) {
      myType = type;
      myCondition = condition;
      myPresentExpression = presentExpression;
      myTypeArgument = TypeConversionUtil.isPrimitive(type) ? "" : "<" + type + ">";
    }

    @Override
    public String getType() {
      return OptionalUtil.getOptionalClass(myType) + myTypeArgument;
    }

    @Override
    public String getCondition() {
      return myCondition;
    }

    @Override
    public String getTrueBranch() {
      return OptionalUtil.getOptionalClass(myType) + "." + myTypeArgument + "of(" + myPresentExpression + ")";
    }

    @Override
    public String getFalseBranch() {
      return OptionalUtil.getOptionalClass(myType) + "." + myTypeArgument + "empty()";
    }

    public Plain unwrap(String absentExpression) {
      return new Plain(myType, myCondition, myPresentExpression, absentExpression);
    }
  }
}
