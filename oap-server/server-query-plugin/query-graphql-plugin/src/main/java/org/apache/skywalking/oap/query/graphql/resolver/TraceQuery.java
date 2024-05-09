/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.oap.query.graphql.resolver;

import graphql.kickstart.tools.GraphQLQueryResolver;
import com.google.common.base.Strings;
import java.io.IOException;
// import java.util.List;
import java.util.Set;
// import java.util.concurrent.atomic.AtomicReference;
import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.UnexpectedException;
import org.apache.skywalking.oap.server.core.analysis.IDManager;
import org.apache.skywalking.oap.server.core.analysis.manual.searchtag.TagType;
import org.apache.skywalking.oap.server.core.query.TagAutoCompleteQueryService;
import org.apache.skywalking.oap.server.core.query.TraceQueryService;
import org.apache.skywalking.oap.server.core.query.input.Duration;
import org.apache.skywalking.oap.server.core.query.input.TraceQueryCondition;
import org.apache.skywalking.oap.server.core.query.type.BasicTrace;
import org.apache.skywalking.oap.server.core.query.type.Pagination;
import org.apache.skywalking.oap.server.core.query.type.QueryOrder;
import org.apache.skywalking.oap.server.core.query.type.Trace;
import org.apache.skywalking.oap.server.core.query.type.TraceBrief;
import org.apache.skywalking.oap.server.core.query.type.TraceState;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
// import org.apache.skywalking.oap.server.library.util.StringUtil;

import static java.util.Objects.isNull;

public class TraceQuery implements GraphQLQueryResolver {

    private final ModuleManager moduleManager;
    private TraceQueryService queryService;
    private TagAutoCompleteQueryService tagQueryService;

    public TraceQuery(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    private TraceQueryService getQueryService() {
        if (queryService == null) {
            this.queryService = moduleManager.find(CoreModule.NAME).provider().getService(TraceQueryService.class);
        }
        return queryService;
    }

    private TagAutoCompleteQueryService getTagQueryService() {
        if (tagQueryService == null) {
            this.tagQueryService = moduleManager.find(CoreModule.NAME).provider().getService(TagAutoCompleteQueryService.class);
        }
        return tagQueryService;
    }

    // private String getParentServerCode(List<String> traceIds, String segmentId, String endpointName) {
    //     AtomicReference<String> serviceCode = new AtomicReference<>("");
    //     try {
    //         if (traceIds == null || traceIds.isEmpty()) {
    //             return null;
    //         }
    //         Trace trace = queryTrace(traceIds.get(0));
    //         if (trace != null) {
    //             AtomicReference<String> parentSegmentId = new AtomicReference<>("");
    //             trace.getSpans().forEach(span -> {
    //                 if (segmentId.equals(span.getSegmentId()) && endpointName.equals(span.getEndpointName())) {
    //                     parentSegmentId.set(span.getSegmentParentSpanId());
    //                 }
    //             });
    //             trace.getSpans().forEach(span -> {
    //                 if (parentSegmentId.get().equals(span.getSegmentSpanId())) {
    //                     serviceCode.set(span.getServiceCode());
    //                 }
    //             });
    //             if (StringUtil.isEmpty(serviceCode.get())) {
    //                 trace.getSpans().forEach(span -> {
    //                     if (segmentId.equals(span.getSegmentId())) {
    //                         serviceCode.set(span.getServiceCode());
    //                     }
    //                 });
    //             }
    //         }
    //     } catch (IOException e) {
    //         throw new UnexpectedException("query trace error", e);
    //     }
    //     return serviceCode.get();
    // }

//     public TraceMetrics queryTraceBasicMetrics(final TraceQueryCondition condition) throws IOException {
//         String traceId = Const.EMPTY_STRING;
//
//         if (!Strings.isNullOrEmpty(condition.getTraceId())) {
//             traceId = condition.getTraceId();
//         } else if (isNull(condition.getQueryDuration())) {
//             throw new UnexpectedException("The condition must contains either queryDuration or traceId.");
//         }
//
//         int minDuration = condition.getMinTraceDuration();
//         int maxDuration = condition.getMaxTraceDuration();
//         String endpointId = condition.getEndpointId();
//         TraceState traceState = condition.getTraceState();
//         QueryOrder queryOrder = condition.getQueryOrder();
//         Pagination pagination = condition.getPaging();
//         TraceMetrics traceMetrics = new TraceMetrics();
//         TraceBrief traceBrief = getQueryService().queryBasicTraces(
//                 condition.getServiceId(), condition.getServiceInstanceId(), endpointId, traceId, minDuration,
//                 maxDuration, traceState, queryOrder, pagination, condition.getQueryDuration(), condition.getTags()
//         );
//         traceMetrics.setTotal(traceBrief.getTotal());
//         List<BasicTrace> traces = traceBrief.getTraces();
//         traces.forEach(e -> {
//             TraceBasicMetric traceBasicMetric = new TraceBasicMetric();
//             traceBasicMetric.setSegmentId(e.getSegmentId());
//             traceBasicMetric.setDuration(e.getDuration());
//             traceBasicMetric.setStart(e.getStart());
// //            traceBasicMetric.setError(e.isError());
//             traceBasicMetric.setError(Boolean.TRUE.equals(isSegmentError(e.getTraceIds(), e.getSegmentId(), e.getEndpointNames().get(0))));
//             traceBasicMetric.setServiceInstanceName(getServiceInstanceName(e.getTraceIds(), e.getSegmentId(), e.getEndpointNames().get(0)));
//             traceBasicMetric.setParentServerCode(getParentServerCode(e.getTraceIds(), e.getSegmentId(), e.getEndpointNames().get(0)));
//             traceBasicMetric.getTraceIds().addAll(e.getTraceIds());
//             traceBasicMetric.getEndpointNames().addAll(e.getEndpointNames());
//             // if only query segment error request
//             if (traceState == TraceState.ERROR && !traceBasicMetric.isError()) {
//                 return;
//             }
//             traceMetrics.getTraceMetrics().add(traceBasicMetric);
//         });
//         return traceMetrics;
//     }

    // private Boolean isSegmentError(List<String> traceIds, String segmentId, String endpointName) {
    //     AtomicReference<Boolean> error = new AtomicReference<>(false);
    //     try {
    //         if (traceIds == null || traceIds.size() == 0) {
    //             return null;
    //         }
    //         Trace trace = queryTrace(traceIds.get(0));
    //         if (trace != null) {
    //             trace.getSpans().forEach(span -> {
    //                 if (segmentId.equals(span.getSegmentId()) && endpointName.equals(span.getEndpointName())) {
    //                     error.set(span.isError());
    //                 }
    //             });
    //         }
    //     } catch (IOException e) {
    //         throw new UnexpectedException("query trace error", e);
    //     }
    //     return error.get();
    // }

    // private String getServiceInstanceName(List<String> traceIds, String segmentId, String endpointName) {
    //     AtomicReference<String> serviceInstanceName = new AtomicReference<>("");
    //     try {
    //         if (traceIds == null || traceIds.size() == 0) {
    //             return null;
    //         }
    //         Trace trace = queryTrace(traceIds.get(0));
    //         if (trace != null) {
    //             trace.getSpans().forEach(span -> {
    //                 if (segmentId.equals(span.getSegmentId()) && endpointName.equals(span.getEndpointName())) {
    //                     serviceInstanceName.set(span.getServiceInstanceName());
    //                 }
    //             });
    //         }
    //     } catch (IOException e) {
    //         throw new UnexpectedException("query trace error", e);
    //     }
    //     return serviceInstanceName.get();
    // }

    public TraceBrief queryBasicTraces(final TraceQueryCondition condition) throws IOException {
        String traceId = Const.EMPTY_STRING;

        if (!Strings.isNullOrEmpty(condition.getTraceId())) {
            traceId = condition.getTraceId();
        } else if (isNull(condition.getQueryDuration())) {
            throw new UnexpectedException("The condition must contains either queryDuration or traceId.");
        }

        int minDuration = condition.getMinTraceDuration();
        int maxDuration = condition.getMaxTraceDuration();
        String endpointId = condition.getEndpointId();
        TraceState traceState = condition.getTraceState();
        QueryOrder queryOrder = condition.getQueryOrder();
        Pagination pagination = condition.getPaging();

        return getQueryService().queryBasicTraces(
            condition.getServiceId(), condition.getServiceInstanceId(), endpointId, traceId, minDuration,
            maxDuration, traceState, queryOrder, pagination, condition.getQueryDuration(), condition.getTags()
        );
    }

    public BasicTrace queryServiceLastAccessInfo(final Duration duration, final String serviceName) throws IOException {
        TraceQueryCondition condition = new TraceQueryCondition();

        condition.setQueryDuration(duration);
        condition.setTraceState(TraceState.ALL);
        condition.setPaging(new Pagination(1, 1));
        condition.setQueryOrder(QueryOrder.BY_START_TIME);
        condition.setServiceId(IDManager.ServiceID.buildId(serviceName, true));

        TraceBrief traces = queryBasicTraces(condition);
        if (!traces.getTraces().isEmpty()) {
            return traces.getTraces().get(0);
        } 
        return new BasicTrace();
    }

    public Trace queryTrace(final String traceId) throws IOException {
        return getQueryService().queryTrace(traceId);
    }

    public Set<String> queryTraceTagAutocompleteKeys(final Duration queryDuration) throws IOException {
        return getTagQueryService().queryTagAutocompleteKeys(TagType.TRACE, queryDuration);
    }

    public Set<String> queryTraceTagAutocompleteValues(final String tagKey, final Duration queryDuration) throws IOException {
        return getTagQueryService().queryTagAutocompleteValues(TagType.TRACE, tagKey, queryDuration);
    }
}
