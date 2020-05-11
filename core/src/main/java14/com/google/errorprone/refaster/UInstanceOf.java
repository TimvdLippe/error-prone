/*
 * Copyright 2013 The Error Prone Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.errorprone.refaster;

import static com.google.errorprone.refaster.Unifier.unifications;

import com.google.auto.value.AutoValue;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.TreeVisitor;
import com.sun.tools.javac.tree.JCTree.JCInstanceOf;
import com.sun.tools.javac.tree.JCTree.JCPattern;
import javax.annotation.Nullable;

/**
 * {@link UTree} representation of a {@link InstanceOfTree}.
 *
 * <p>JDK 14 introduces {@link #getPattern()} for pattern matching for {@code instanceof} in <a
 * href="https://github.com/openjdk/jdk/commit/229e0d16313b10932b9ce7506d84096696983699"
 * >https://github.com/openjdk/jdk/commit/229e0d16313b10932b9ce7506d84096696983699</a>
 *
 * @author lowasser@google.com (Louis Wasserman)
 */
@AutoValue
abstract class UInstanceOf extends UExpression implements InstanceOfTree {
  public static UInstanceOf create(UExpression expression, UTree<?> type) {
    return new AutoValue_UInstanceOf(null, expression, type);
  }

  @Nullable
  @Override
  public abstract JCPattern getPattern();

  @Override
  public abstract UExpression getExpression();

  @Override
  public abstract UTree<?> getType();

  @Override
  public <R, D> R accept(TreeVisitor<R, D> visitor, D data) {
    return visitor.visitInstanceOf(this, data);
  }

  @Override
  public Kind getKind() {
    return Kind.INSTANCE_OF;
  }

  @Override
  public JCInstanceOf inline(Inliner inliner) throws CouldNotResolveImportException {
    return inliner.maker().TypeTest(getExpression().inline(inliner), getType().inline(inliner));
  }

  @Override
  @Nullable
  public Choice<Unifier> visitInstanceOf(InstanceOfTree instanceOf, @Nullable Unifier unifier) {
    return getExpression()
        .unify(instanceOf.getExpression(), unifier)
        .thenChoose(unifications(getType(), instanceOf.getType()));
  }
}
