/* Copyright 2014-2015 Allen Arakaki.  All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ikakara.awsinstance.platform

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException

import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions

import com.amazonaws.services.cloudformation.AmazonCloudFormation
import com.amazonaws.services.cloudformation.model.CreateStackRequest
import com.amazonaws.services.cloudformation.model.DeleteStackRequest
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesRequest
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest
import com.amazonaws.services.cloudformation.model.Stack
import com.amazonaws.services.cloudformation.model.StackResource
import com.amazonaws.services.cloudformation.model.StackStatus

import ikakara.awsinstance.aws.AWSInstance
import ikakara.awsinstance.util.PrintlnUtil

// https://github.com/aws/aws-sdk-java/blob/master/src/samples/AwsCloudFormation/CloudFormationSample.java
@CompileStatic
@Slf4j
class AwsConfigurationService {
  static transactional = false

  def grailsApplication

  String listStack(Region region) {
    try {
      if(!region) {
        // default to useast
        region = Region.getRegion(Regions.US_EAST_1)
      }

      AmazonCloudFormation stackbuilder = AWSInstance.CLOUDFORMATION_CLIENT()
      stackbuilder.setRegion(region)

      return null
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("listStack ${region}", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("listStack ${region}", ace)
    }

    return ''
  }

  String getStack(String name, Region region) {
    try {
      if(!region) {
        region = Region.getRegion(Regions.US_EAST_1)
      }

      return null
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("getStack ${region}", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("getStack ${region}", ace)
    }

    return ''
  }

  String createStack(Region region) {
    try {
      if(!region) {
        region = Region.getRegion(Regions.US_EAST_1)
      }
      return null
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("createStack ${region}", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("createStack ${region}", ace)
    }

    return ''
  }

  String deleteStack(Region region) {
    try {
      if(!region) {
        region = Region.getRegion(Regions.US_EAST_1)
      }
      return null
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("deleteStack ${region}", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("deleteStack ${region}", ace)
    }

    return ''
  }

  // Wait for a stack to complete transitioning
  // End stack states are:
  //    CREATE_COMPLETE
  //    CREATE_FAILED
  //    DELETE_FAILED
  //    ROLLBACK_FAILED
  // OR the stack no longer exists
  public String waitForCompletion(AmazonCloudFormation stackbuilder, String stackName) throws Exception {

    DescribeStacksRequest wait = new DescribeStacksRequest()
    wait.setStackName(stackName)
    Boolean completed = false
    String  stackStatus = "Unknown"
    String  stackReason = ""

    log.debug("Waiting")

    while (!completed) {
      List<Stack> stacks = stackbuilder.describeStacks(wait).getStacks()
      if (stacks.isEmpty()) {
        completed   = true
        stackStatus = "NO_SUCH_STACK"
        stackReason = "Stack has been deleted"
      } else {
        for (Stack stack : stacks) {
          if (stack.getStackStatus().equals(StackStatus.CREATE_COMPLETE.toString()) ||
            stack.getStackStatus().equals(StackStatus.CREATE_FAILED.toString()) ||
            stack.getStackStatus().equals(StackStatus.ROLLBACK_FAILED.toString()) ||
            stack.getStackStatus().equals(StackStatus.DELETE_FAILED.toString())) {
            completed = true
            stackStatus = stack.getStackStatus()
            stackReason = stack.getStackStatusReason()
          }
        }
      }

      // Show we are waiting
      log.debug(".")

      // Not done yet so sleep for 10 seconds.
      if (!completed) Thread.sleep(10000)
    }

    // Show we are done
    log.debug("done\n")

    return stackStatus + " (" + stackReason + ")"
  }
}
