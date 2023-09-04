package xyz.unpunished.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HexAction {
    
    private final HexActionType actionType;
    private byte[] value;
    
}
