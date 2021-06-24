package com.github.bryx.workflow.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * @Author jameswu
 * @Date 2021/6/3
 **/
@Data
public class PageDto<T>{

    private Long current = 0l;

    private Long size = Long.MAX_VALUE;

    public Page<T> page(){
        Page<T> page = new Page<>();
        page.setCurrent(this.current);
        page.setSize(this.size);
        return page;
    }
}
