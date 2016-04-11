package io.fabric8.zipkin.examples.helloworld;

import org.springframework.cloud.stream.converter.AbstractFromMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import zipkin.Codec;
import zipkin.Span;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class ZipkinSpanListMessageConverter extends AbstractFromMessageConverter {

  protected ZipkinSpanListMessageConverter() {
    super(Arrays.asList(MimeTypeUtils.APPLICATION_JSON, MimeType.valueOf("application/x-thrift")));
  }

  @Override
  protected Class<?>[] supportedTargetTypes() {
    return new Class<?>[]{String.class};
  }

  @Override
  protected Class<?>[] supportedPayloadTypes() {
    return new Class<?>[]{List.class};
  }

  @Override
  protected boolean supports(Class<?> aClass) {
    return aClass == null || List.class != aClass;
  }

  @Override
  protected Object convertFromInternal(Message<?> message, Class<?> targetClass, Object conversionHint) {
    Object payload = message.getPayload();
    if (!(payload instanceof List)) {
      return null;
    }
    List spanList = (List) payload;
    if (spanList.isEmpty()) {
      return null;
    }
    Class colType = spanList.iterator().next().getClass();
    if (colType != Span.class) {
      return null;
    }
    return new String(Codec.JSON.writeSpans((List<Span>) spanList), StandardCharsets.UTF_8);
  }
}
