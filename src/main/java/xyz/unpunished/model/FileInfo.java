package xyz.unpunished.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class FileInfo {
    
    private int size;
    private FileType type;
    
}
