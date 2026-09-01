package com.app.modules.sensor.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class SseEmitterService {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    public SseEmitter createConnection(){
        SseEmitter emitter = new SseEmitter(1800000L);
        emitters.add(emitter);
        log.info("[SSE] Khách hàng mới kết nối. Tổng số kết nối: {}", emitters.size());

        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            log.info("[SSE] Kết nối hoàn tất. Còn lại: {}", emitters.size());
        });

        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            log.info("[SSE] Kết nối timeout. Còn lại: {}", emitters.size());
        });

        emitter.onError((e) -> {
            emitters.remove(emitter);
            log.warn("[SSE] Lỗi kết nối client. Đã loại bỏ. Còn lại: {}", emitters.size());
        });
        try {
            emitter.send(SseEmitter.event()
                    .name("INIT_STREAM")
                    .data("{\"message\":\"SSE Stream Connected Successfully\"}", MediaType.APPLICATION_JSON));
        }
        catch (Exception e) {
            emitters.remove(emitter);
        }
        return emitter;
    }

    public void broadcastEvent(String eventName, Object data) {
        for(SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data, MediaType.APPLICATION_JSON));
            }
            catch (Exception e){
                emitters.remove(emitter);
                log.warn("[SSE] Lỗi gửi dữ liệu tới 1 client, đã tự động gỡ bỏ emitter.");
            }
        }
    }
}
