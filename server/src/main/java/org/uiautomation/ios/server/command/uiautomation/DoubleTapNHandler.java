/*
 * Copyright 2012-2013 eBay Software Foundation and ios-driver committers
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.uiautomation.ios.server.command.uiautomation;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.uiautomation.ios.communication.WebDriverLikeRequest;
import org.uiautomation.ios.server.IOSServerManager;
import org.uiautomation.ios.server.command.UIAScriptHandler;
import org.uiautomation.ios.server.utils.CoordinateUtils;
import org.uiautomation.ios.server.utils.JSTemplate;
import org.uiautomation.ios.wkrdp.RemoteIOSWebDriver;
import org.uiautomation.ios.wkrdp.model.NodeId;
import org.uiautomation.ios.wkrdp.model.RemoteWebNativeBackedElement;

public class DoubleTapNHandler extends UIAScriptHandler {

  private static final JSTemplate plainTemplate = new JSTemplate(
      "var element = UIAutomation.cache.get(%:nodeId$d, false);" +
      "var status = Number(!element.isVisible());" +
      "if (status == 0) {" +
        "UIATarget.localTarget().doubleTap(element);" +
      "}" +
      "UIAutomation.createJSONResponse('%:sessionId$s', status, '');",
      "sessionId", "nodeId");
  private static final JSTemplate nativeTemplate = new JSTemplate(
      "UIATarget.localTarget().doubleTap({x:%:tapX$d, y:%:tapY$d});" +
      "UIAutomation.createJSONResponse('%:sessionId$s',0,'')",
      "sessionId", "tapX", "tapY");

  public DoubleTapNHandler(IOSServerManager driver, WebDriverLikeRequest request) throws Exception {
    super(driver, request);

    JSONObject payload = request.getPayload();
    String elementId = payload.optString("element");
    if (RemoteIOSWebDriver.isPlainElement(elementId)) {
      NodeId nodeId = RemoteIOSWebDriver.plainNodeId(elementId);
      setJS(plainTemplate.generate(request.getSession(), nodeId.getId()));
    } else {
      Dimension screenSize = getNativeDriver().getScreenSize();
      RemoteWebNativeBackedElement element = (RemoteWebNativeBackedElement) getWebDriver().createElement(elementId);
      Point tapPoint = element.getLocation();
      tapPoint = CoordinateUtils.forcePointOnScreen(tapPoint, screenSize);
      setJS(nativeTemplate.generate(request.getSession(), tapPoint.getX(), tapPoint.getY()));
    }
  }

  @Override
  public JSONObject configurationDescription() throws JSONException {
    return noConfigDefined();
  }
}
