package org.dromara.property.charge.payment.controller;
import cn.dev33.satoken.annotation.*;
import org.dromara.property.charge.payment.domain.bo.PaymentCreateBo;
import org.dromara.property.charge.payment.domain.bo.PaymentReverseBo;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
class PaymentControllerSecurityTest {
 @Test void createRequiresAddAndAllocatePermissions()throws Exception{SaCheckPermission p=PaymentController.class.getMethod("create",PaymentCreateBo.class).getAnnotation(SaCheckPermission.class);assertNotNull(p);assertEquals(SaMode.AND,p.mode());assertEquals(Set.of("property:payment:add","property:payment:allocate"),Set.of(p.value()));}
 @Test void roomOptionsRequiresAddAndAllocatePermissions()throws Exception{SaCheckPermission p=PaymentController.class.getMethod("roomOptions",Long.class).getAnnotation(SaCheckPermission.class);assertNotNull(p);assertEquals(SaMode.AND,p.mode());assertEquals(Set.of("property:payment:add","property:payment:allocate"),Set.of(p.value()));}
 @Test void reverseRequiresDedicatedPermission()throws Exception{SaCheckPermission p=PaymentController.class.getMethod("reverse",Long.class,PaymentReverseBo.class).getAnnotation(SaCheckPermission.class);assertNotNull(p);assertArrayEquals(new String[]{"property:payment:reverse"},p.value());}
}
