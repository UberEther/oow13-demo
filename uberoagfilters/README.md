oow13-demo - Uber OAG Filters
=============================

To discuss - deployment issues if you remove parameters

Placement of files (not always clear in Oracle tutorial)

Fact that we hard-coded variables but really should make them configurable

Discuss TRACE logger object


VARIABLE NAMES:
uber.userAgent - Input to agent filter
uber.userAgent.browser - Output from agent filter
uber.userAgent.browser.ver - Output from agent filter
uber.userAgent.platform - Output from agent filter

accesstoken - Input to OAuth2 filter
uber.oauth2.access_token - Output clear text string access token output from OAuth2 filter
uber.oauth2.id - ID value in DB for access token output from OAuth2 filter

OneJar used - possible downstream risk if multiple plugins use same dependencies


