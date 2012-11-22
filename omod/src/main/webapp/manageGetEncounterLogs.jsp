<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>

<b class="boxHeader"><spring:message code="rheashradapter.getEncounterLogTable"/></b>
<div class="box">
<br />
<table cellpadding="4" cellspacing="0" style="border: 1px solid #918E90">
	<c:if test="${fn:length(getEncounterLogs) > 0}">
	<tr style="background-color: #CCCCCC">
		<th valign="top"><spring:message code="rheashradapter.getRequestId"/></th>
		<th valign="top"><spring:message code="rheashradapter.patientId"/></th>
		<th valign="top"><spring:message code="rheashradapter.encounterUniqueId"/></th>
		<th valign="top"><spring:message code="rheashradapter.enterpriseLocationId"/></th>
		<th valign="top"><spring:message code="rheashradapter.dateStart"/></th>
		<th valign="top"><spring:message code="rheashradapter.dateEnd"/></th>
		<th valign="top"><spring:message code="rheashradapter.logTime"/></th>
		<th valign="top"><spring:message code="rheashradapter.result"/></th>
		<th valign="top"><spring:message code="rheashradapter.error"/></th>
	</tr>
	</c:if>
	<c:forEach var="getEncounterLog" items="${getEncounterLogs}" varStatus="varStatus">
	<tr <c:if test="${varStatus.index % 2 == 0}">class='evenRow'</c:if>>
		<td valign="top">${getEncounterLog.getRequestId}</td>
		<td valign="top">${getEncounterLog.patientId}</td>
		<td valign="top">${getEncounterLog.encounterUniqueId}</td>
		<td valign="top">${getEncounterLog.enterpriseLocationId}</td>
		<td valign="top">${getEncounterLog.dateStart}</td>
		<td valign="top">${getEncounterLog.dateEnd}</td>
		<td valign="top">${getEncounterLog.logTime}</td>
		<td valign="top"><a href="<openmrs:contextPath />/module/rheashradapter/getRequestTemplate.form?getRequestId=${getEncounterLog.getRequestId}">
		${getEncounterLog.result}
		</a></td>
		<td valign="top">${getEncounterLog.error}</td>
	</tr>
	</c:forEach>
</table>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>