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

import grails.compiler.GrailsCompileStatic
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.InitializingBean

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient
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
@GrailsCompileStatic
@Slf4j
class AwsConfigurationService implements InitializingBean {
  static transactional = false

  public static Region DEFAULT_REGION

  def grailsApplication

  @GrailsCompileStatic(TypeCheckingMode.SKIP)
  void afterPropertiesSet() {
    def regionStr = grailsApplication.config.grails.plugin.awsinstance?.defaultRegion
    DEFAULT_REGION = Region.getRegion(Regions.valueOf(regionStr))
  }

  List<Stack> listStack(Region region=DEFAULT_REGION) {
    AmazonCloudFormationClient stackbuilder = (AmazonCloudFormationClient)AWSInstance.CLOUDFORMATION_CLIENT()
    stackbuilder.withRegion(region)

    try {
      // Show all the stacks for this account along with the resources for each stack
      return stackbuilder.describeStacks(new DescribeStacksRequest()).getStacks()
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("listStack ${region}", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("listStack ${region}", ace)
    }

    return null
  }

  List<StackResource> findStack(String stackName, Region region=DEFAULT_REGION) {
    AmazonCloudFormationClient stackbuilder = (AmazonCloudFormationClient)AWSInstance.CLOUDFORMATION_CLIENT()
    stackbuilder.withRegion(region)

    try {
      DescribeStackResourcesRequest stackResourceRequest = new DescribeStackResourcesRequest().withStackName(stackName)
      return stackbuilder.describeStackResources(stackResourceRequest).getStackResources()
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("getStack ${region} ${stackName}", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("getStack ${region} ${stackName}", ace)
    }

    return null
  }

  boolean createStack(String stackName, String template, Region region=DEFAULT_REGION) {
    AmazonCloudFormationClient stackbuilder = (AmazonCloudFormationClient)AWSInstance.CLOUDFORMATION_CLIENT()
    stackbuilder.withRegion(region)

    try {
      CreateStackRequest createRequest = new CreateStackRequest().withStackName(stackName).withTemplateBody(template)
      log.debug("Creating a stack called " + createRequest.getStackName() + ".")
      stackbuilder.createStack(createRequest)

      return true
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("createStack ${region} ${stackName}", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("createStack ${region} ${stackName}", ace)
    }

    return false
  }

  boolean deleteStack(String stackName, Region region=DEFAULT_REGION) {
    AmazonCloudFormationClient stackbuilder = (AmazonCloudFormationClient)AWSInstance.CLOUDFORMATION_CLIENT()
    stackbuilder.withRegion(region)

    try {
      // Delete the stack
      DeleteStackRequest deleteRequest = new DeleteStackRequest().withStackName(stackName)
      log.debug("Deleting the stack called " + deleteRequest.getStackName() + ".")
      stackbuilder.deleteStack(deleteRequest)
      return true
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("deleteStack ${region} ${stackName}", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("deleteStack ${region} ${stackName}", ace)
    }

    return false
  }

  // Wait for a stack to complete transitioning
  // End stack states are:
  //    CREATE_COMPLETE
  //    CREATE_FAILED
  //    DELETE_FAILED
  //    ROLLBACK_FAILED
  // OR the stack no longer exists
  String waitForCompletion(String stackName, Region region=DEFAULT_REGION) {
    AmazonCloudFormationClient stackbuilder = (AmazonCloudFormationClient)AWSInstance.CLOUDFORMATION_CLIENT()
    stackbuilder.withRegion(region)

    DescribeStacksRequest wait = new DescribeStacksRequest().withStackName(stackName)

    Boolean completed = false
    String  stackStatus = "Unknown"
    String  stackReason = ""

    log.debug("Waiting")

    try {
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

        // Not done yet so sleep for 1 seconds.  We should do an exponential backoff
        if (!completed) {
          Thread.sleep(1000)
        }
      }
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("waitForCompletion ${region} ${stackName}", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("waitForCompletion ${region} ${stackName}", ace)
    }

    // Show we are done
    log.debug("done\n")

    return stackStatus + " (" + stackReason + ")"
  }
}
