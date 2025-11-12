import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.awt.print.Pageable;

@GetMapping("/all")
public Class<?> getAllRequests(
        @RequestHeader("X-Sharer-User-Id") Long userId,
        @RequestParam(defaultValue = "0") int from,
        @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = (Pageable) PageRequest.of(from / size, size);
    Object requestService = null;
    return requestService.getClass();
}

public void main() {
}