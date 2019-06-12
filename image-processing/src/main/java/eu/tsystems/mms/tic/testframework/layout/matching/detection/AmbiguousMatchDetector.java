/*
 * Created on 03.13.2014
 *
 * Copyright(c) 2011 - 2014 T-Systems Multimedia Solutions GmbH
 * Riesaer Str. 5, 01129 Dresden
 * All rights reserved.
 */
package eu.tsystems.mms.tic.testframework.layout.matching.detection;

import eu.tsystems.mms.tic.testframework.constants.FennecProperties;
import eu.tsystems.mms.tic.testframework.layout.matching.LayoutMatch;
import eu.tsystems.mms.tic.testframework.layout.matching.error.AmbiguousMatchError;
import eu.tsystems.mms.tic.testframework.layout.matching.error.LayoutFeature;
import eu.tsystems.mms.tic.testframework.layout.matching.graph.DistanceGraph;
import eu.tsystems.mms.tic.testframework.layout.matching.graph.Edge;
import eu.tsystems.mms.tic.testframework.layout.matching.graph.MatchNode;

import java.util.LinkedList;
import java.util.List;

/**
 * User: rnhb
 * Date: 12.06.14
 * <p/>
 * Error detector for cases when a match is matched on several templates.
 */
public class AmbiguousMatchDetector extends FeatureDetector {

    public AmbiguousMatchDetector() {
        ignorePropertyKey = FennecProperties.LAYOUTCHECK_IGNORE_AMBIGUOUS_MATCH;
    }

    @Override
    protected List<LayoutFeature> findFeatures(DistanceGraph distanceGraph, LayoutMatch layoutMatch) {
        List<LayoutFeature> errors = new LinkedList<LayoutFeature>();
        for (MatchNode matchNode : distanceGraph.getMatchNodes()) {
            List<Edge> edgesToTemplateNode = matchNode.getEdgesToTemplateNode();
            if (edgesToTemplateNode.size() > 1) {
                if (!hasEdgeToSameLocation(edgesToTemplateNode, matchNode)) {
                    errors.add(new AmbiguousMatchError(matchNode));
                }
            }
        }
        return errors;
    }
}