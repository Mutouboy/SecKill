//存放主要交互逻辑js代码
//js 模块化

var seckill = {

    //封装秒杀相关ajax的url
    URL: {
        now : function () {
            return '/seckill/time/now';
        },

        exposer:function (seckillId) {
            return '/seckill/'+seckillId+'/exposer';
        },

        execution:function (seckillId,md5) {
            return '/seckill/'+seckillId+'/'+md5+'/execution';
        }
    },

    //验证手机号
    validatePhone: function (phone) {
        if (phone && phone.length == 11 && !isNaN(phone)) {
            return true;
        } else {
            return false;
        }
    },

    handleSeckillKill:function (seckillId,node) {
        //处理秒杀逻辑
        node.html('<button class="btn btn-primary btn-lg" id="killBtn">开始秒杀</button>');
        $.post(seckill.URL.exposer(seckillId),{},function (result) {
            //在回调函数中执行交互流程
            if (result && result['success']){
                var exposer = result['data'];
                if (exposer['exposed']){
                    //如果开启秒杀
                    //获取秒杀地址
                    var md5 = exposer['md5'];
                    var killUrl = seckill.URL.execution(seckillId,md5);
                    console.log("killUrl: "+killUrl);
                    //只绑定一次点击事件，防止连续点击造成服务器压力过大
                    $('#killBtn').one('click',function () {
                        //执行秒杀请求
                        $(this).addClass('disabled');//禁用按钮

                        //发送秒杀请求
                        $.post(killUrl,{},function (result) {
                            if(result && result['success']){
                                var killResult = result['data'];
                                var state = killResult['state'];
                                var stateInfo = killResult['stateInfo'];
                                //显示秒杀结果
                                node.html('<span class="label label-success">'+stateInfo+'</span>');

                            }
                        })
                    })
                    //显示节点
                   // node.show;
                }else {
                    //未开启(本地和服务器时间有偏移)
                    var now = exposer['now'];
                    var start = exposer['start'];
                    var end = exposer['end'];
                    //重新进入计时逻辑
                    seckill.countdown(seckillId,now,start,end);
                }
            }else {
                console.log("exposer = "+exposer);
            }
        })
    }
    ,

    //计时事件
    countdown :function(seckillId,nowTime,startTime,endTime){
        //时间判断
        var seckillBox = $('#seckill-box');
        if (nowTime>endTime){
            //秒杀结束
            seckillBox.html('秒杀结束！');
        }else if (nowTime<startTime){
            //秒杀未开始,计时事件绑定
            var killTime=new Date(startTime+1000);

            seckillBox.countdown(killTime,function (event) {
                //时间格式
                var format = event.strftime('秒杀倒计时： %D天 %H时 %M分 %S秒');
                seckillBox.html(format);
            }).on('finish.countdown',function () {
                //时间完成后回调事件
                //获取秒杀地址，控制显示逻辑，执行秒杀
                seckill.handleSeckillKill(seckillId,seckillBox);
            })
        }else {
            //秒杀开始
            seckill.handleSeckillKill(seckillId,seckillBox);
        }
    },

    //详情页秒杀逻辑
    detail: {

        //详情页初始化
        init: function (params) {
            //手机验证和登录，计时交互
            //规划我们的交互流程
            //在cookie中查找手机号
            var killPhone = $.cookie('killPhone');
            var startTime = params['startTime'];
            var endTime = params['endTime'];
            var seckillId = params['seckillId'];

            //验证手机号
            if (!seckill.validatePhone(killPhone)) {
                //绑定phone
                //控制输出
                var killPhoneModal = $('#killPhoneModal');
                //显示弹出层
                killPhoneModal.modal({
                    show: true,//显示弹出层
                    backdrop: 'static', //禁止位置关闭
                    keyboard: false //关闭键盘事件
                });

                $('#killPhoneBtn').click(function () {
                    var inputPhone = $('#killPhoneKey').val();
                    console.log('inputPhone = '+inputPhone);//TODO
                    if (seckill.validatePhone(inputPhone)) {
                        //电话写入cookie
                        $.cookie('killPhone', inputPhone, {expires: 7, path: '/seckill'});
                        //刷新页面
                        window.location.reload();
                    }else {
                        //先隐藏再放内容再显示
                        $('#killPhoneMessage').hide().html('<label class="label label-danger">手机号报错</label>').show(300);
                    }
                });
            }
            //已经登录

            //计时交互
            $.get(seckill.URL.now(),{},function(result){
                if (result && result['success']){
                    var nowTime = result['data'];
                    //时间判断
                    seckill.countdown(seckillId,nowTime,startTime,endTime);
                }else {
                    console.log('result = '+result);
                }
            });
        }

    }
}