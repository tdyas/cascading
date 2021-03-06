/*
 * Copyright (c) 2007-2014 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.cascading.org/
 *
 * This file is part of the Cascading project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cascading.flow.planner.iso.transformer;

import java.util.List;
import java.util.Set;

import cascading.flow.FlowElement;
import cascading.flow.planner.graph.ElementGraph;
import cascading.flow.planner.graph.ElementGraphs;
import cascading.flow.planner.iso.expression.ElementCapture;
import cascading.flow.planner.iso.expression.ExpressionGraph;
import cascading.flow.planner.iso.finder.Match;
import org.jgrapht.Graphs;

/**
 *
 */
public class InsertionGraphTransformer extends MutateGraphTransformer
  {
  public enum Insertion
    {
      Before,
      After,
      BeforeEachEdge,
      AfterEachEdge
    }

  private Insertion insert = Insertion.After;
  private final String factoryName;
  private ElementCapture capture = ElementCapture.Primary;

  public InsertionGraphTransformer( ExpressionGraph expressionGraph, String factoryName )
    {
    this( expressionGraph, ElementCapture.Primary, factoryName );
    }

  public InsertionGraphTransformer( ExpressionGraph expressionGraph, String factoryName, Insertion insert )
    {
    this( expressionGraph, ElementCapture.Primary, factoryName, insert );
    }

  public InsertionGraphTransformer( ExpressionGraph expressionGraph, ElementCapture capture, String factoryName )
    {
    this( expressionGraph, capture, factoryName, Insertion.After );
    }

  public InsertionGraphTransformer( ExpressionGraph expressionGraph, ElementCapture capture, String factoryName, Insertion insert )
    {
    super( expressionGraph );

    this.insert = insert;

    if( capture != null )
      this.capture = capture;

    this.factoryName = factoryName;

    if( factoryName == null )
      throw new IllegalArgumentException( "factoryName may not be null" );
    }

  public InsertionGraphTransformer( GraphTransformer graphTransformer, ExpressionGraph filter, String factoryName )
    {
    this( graphTransformer, filter, ElementCapture.Primary, factoryName );
    }

  public InsertionGraphTransformer( GraphTransformer graphTransformer, ExpressionGraph filter, ElementCapture capture, String factoryName )
    {
    this( graphTransformer, filter, capture, factoryName, Insertion.After );
    }

  public InsertionGraphTransformer( GraphTransformer graphTransformer, ExpressionGraph filter, ElementCapture capture, String factoryName, Insertion insert )
    {
    super( graphTransformer, filter );

    this.insert = insert;

    if( capture != null )
      this.capture = capture;

    this.factoryName = factoryName;

    if( factoryName == null )
      throw new IllegalArgumentException( "factoryName may not be null" );
    }

  @Override
  protected boolean transformGraphInPlaceUsing( Transformed<ElementGraph> transformed, ElementGraph graph, Match match )
    {
    Set<FlowElement> insertions = match.getCapturedElements( capture );

    if( insertions.isEmpty() )
      return false;

    ElementFactory elementFactory = transformed.getPlannerContext().getElementFactoryFor( factoryName );

    for( FlowElement flowElement : insertions )
      {
      switch( insert )
        {
        case Before:

          ElementGraphs.insertFlowElementBefore( graph, flowElement, elementFactory.create( graph, flowElement ) );
          break;

        case After:

          ElementGraphs.insertFlowElementAfter( graph, flowElement, elementFactory.create( graph, flowElement ) );
          break;

        case BeforeEachEdge:
          List<FlowElement> predecessors = Graphs.predecessorListOf( graph, flowElement );

          for( FlowElement predecessor : predecessors )
            ElementGraphs.insertFlowElementAfter( graph, predecessor, elementFactory.create( graph, predecessor ) );

          break;

        case AfterEachEdge:

          List<FlowElement> successors = Graphs.successorListOf( graph, flowElement );

          for( FlowElement successor : successors )
            ElementGraphs.insertFlowElementBefore( graph, successor, elementFactory.create( graph, successor ) );

          break;
        }
      }

    return true;
    }
  }
