package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.common.R;
import com.itheima.entity.User;
import com.itheima.service.UserService;
import com.itheima.utils.SMSUtils;
import com.itheima.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *用户登录
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送手机短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号码
        String phone=user.getPhone();

        if(StringUtils.isNotEmpty(phone)){
            //生成随机的4为验证码
            String code = ValidateCodeUtils.generateValidateCode(6).toString();
            log.info("code={}",code);

            //调用阿里云提供的短信服务API完成发送短信
            //SMSUtils.sendMessage("捞鱼炒粉","SMS_460665423",phone,code);

            //将生成的验证码保存到Session
            //session.setAttribute(phone,code);

            //将生成的验证码缓存到redis中，设置有效期5分钟
            stringRedisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);

            return R.success("手机验证码发送成功");
        }
        return R.error("手机验证码发送失败");
    }

    /**
     * 移动端用户登录
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    //前端传过来的数据，有phone和code 参数可以直接用Map（键值对）
    public R<User> login(@RequestBody Map map,HttpSession session){
        log.info(map.toString());
        //获取手机号
        String phone = map.get("phone").toString();

        //获取验证码
        String code = map.get("code").toString();

        //从session中获取保存的验证码
        //Object codeInSession = session.getAttribute(phone);

        //从redis中获取验证码
        String codeInRedis = stringRedisTemplate.opsForValue().get(phone);

        //进行验证码比对（页面提交的验证码和session保存的验证码）
        if(codeInRedis!=null && codeInRedis.equals(code)){
            //如果能够比对成功，说明登录成功
            //判断当前手机号是否为新用户
            LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);

            User user = userService.getOne(queryWrapper);
            if(user==null){
                //如果为新用户，则自动完成注册
                user=new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            //跟员工登录一样，登录后需要把id存到session中，避免过滤器查不到id而进行拦截
            session.setAttribute("user",user.getId());

            //如果用户登录成功，删除redis中缓存的验证码
            stringRedisTemplate.delete(phone);

            return R.success(user);
        }
        return R.error("登陆失败");
    }

    /**
     * 退出登录
     * @param session
     * @return
     */
    @PostMapping("/loginout")
    public R<String> loginOut(HttpSession session){
        //清理Session中保存的当前移动端登录的用户id
        session.removeAttribute("user");
        return R.success("退出成功");
    }
}
