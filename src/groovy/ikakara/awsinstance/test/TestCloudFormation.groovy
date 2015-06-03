/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ikakara.awsinstance.test

/**
 *
 * @author Allen
 */
class TestCloudFormation {
  static String S3_CreateBucket(name) {
    return
"""
{
    "AWSTemplateFormatVersion" : "2010-09-09",
    "Resources" : {
        "${name}" : {
            "Type" : "AWS::S3::Bucket",
            "Properties" : {
               "AccessControl" : "PublicRead"               
            }
        }
    }
}
    """
  }
}

