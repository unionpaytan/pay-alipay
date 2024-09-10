package com.bootpay.xxl.Jobs;

import com.bootpay.common.excetion.TranException;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 关闭
 * */
//@Component
public class TestJobs {
    private static Logger _log = LoggerFactory.getLogger(TestJobs.class);
    @XxlJob("TestJobsHandler")
    public void TestJobsHandler() throws TranException {
        XxlJobHelper.log("XXL-JOB:TestJobsHandler");
    }
}
