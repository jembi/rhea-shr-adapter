<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>

<b class="boxHeader"><spring:message code="rheashradapter.postEncounterLogTable"/></b>
<div class="box">
<br />
<table cellpadding="4" cellspacing="0" style="border: 1px solid #918E90">
	<c:if test="${fn:length(postEncounterLogs) > 0}">
	<tr style="background-color: #CCCCCC">
		<th valign="top"><spring:message code="rheashradapter.requestId"/></th>
		<th valign="top"><spring:message code="rheashradapter.patientId"/></th>
		<th valign="top"><spring:message code="rheashradapter.hl7data"/></th>
		<th valign="top"><spring:message code="rheashradapter.valid"/></th>
		<th valign="top"><spring:message code="rheashradapter.dateCreated"/></th>
		<th valign="top"><spring:message code="rheashradapter.userId"/></th>
		<th valign="top"><spring:message code="rheashradapter.result"/></th>
		<th valign="top"><spring:message code="rheashradapter.error"/></th>
	</tr>
	</c:if>
	<c:forEach var="postEncounterLog" items="${postEncounterLogs}" varStatus="varStatus">
	<tr <c:if test="${varStatus.index % 2 == 0}">class='evenRow'</c:if>>
		<td valign="top">${postEncounterLog.postRequestId}</td>
		<td valign="top">${postEncounterLog.patientId}</td>
		<td valign="top"><a href="<openmrs:contextPath />/module/rheashradapter/postRequestTemplate.form?postRequestId=${postEncounterLog.postRequestId}">
		HL7 Message
		</a></td>
		<td valign="top">${postEncounterLog.valid}</td>
		<td valign="top">${postEncounterLog.dateCreated}</td>
		<td valign="top">${postEncounterLog.userId}</td>
		<td valign="top">${postEncounterLog.result}</td>
		<td valign="top">${postEncounterLog.error}</td>
	</tr>
	</c:forEach>
</table>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>