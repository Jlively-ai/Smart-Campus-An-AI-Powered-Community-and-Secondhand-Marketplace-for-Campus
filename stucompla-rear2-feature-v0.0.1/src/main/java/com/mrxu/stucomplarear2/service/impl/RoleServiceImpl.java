package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrxu.stucomplarear2.entity.Role;
import com.mrxu.stucomplarear2.mapper.RoleMapper;
import com.mrxu.stucomplarear2.service.RoleService;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
}
