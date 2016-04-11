package io.fabric8.zipkin.examples.helloworld;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.metric.SpanMetricReporter;
import org.springframework.cloud.sleuth.zipkin.ZipkinSpanReporter;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.annotation.InboundChannelAdapter;
import zipkin.Span;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@EnableBinding(Source.class)
public class StreamZipkinSpanReporter implements ZipkinSpanReporter {

  private static final Log log = LogFactory.getLog(StreamZipkinSpanReporter.class);

  private SpanMetricReporter spanMetricReporter;

  private Queue<Span> queue = new ConcurrentLinkedQueue<>();

  @Autowired
  public void setSpanMetricReporter(SpanMetricReporter spanMetricReporter) {
    this.spanMetricReporter = spanMetricReporter;
  }

  @InboundChannelAdapter(Source.OUTPUT)
  public List<Span> poll() {
    List<Span> result = new ArrayList<>(this.queue);
    this.queue.clear();
    if (result.isEmpty()) {
      return null;
    }
    this.spanMetricReporter.incrementAcceptedSpans(result.size());
    return result;
  }

  /**
   * Queues the span for collection, or drops it if the queue is full.
   *
   * @param span Span, should not be <code>null</code>.
   */
  @Override
  public void report(Span span) {
    if (span != null && !("message:" + Source.OUTPUT).equals(span.name)) {
      this.queue.add(span);
    }
  }

}
