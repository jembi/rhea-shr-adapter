<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>


<b class="boxHeader"><spring:message code="rheashradapter.getEncounterLogResult"/></b>
<div class="box">
<br />
<table cellpadding="4" cellspacing="0" >
<table>
	<tr>
		<td><b><spring:message code="rheashradapter.getRequestId"/></b></td>
		<td>
			${getEncounterLog.getRequestId}
		</td>
	</tr>
	<tr>
		<td><b><spring:message code="rheashradapter.result"/></b></td>
		<td>
			${getEncounterLog.result}
		</td>	
	</tr>
	<c:if test="${not empty getEncounterLog.errorDetails}">
		<tr>
		<td><b><spring:message code="rheashradapter.errorDetails"/></b></td>
		<td>
			${getEncounterLog.errorDetails}
		</td>
	</tr>
	</c:if>
	<c:if test="${fn:length(getEncounterLog.matchingEncounters) > 0}">
	</tr>
		<tr>
		<td><b><spring:message code="rheashradapter.encountersRetrieved"/></b></td>
		<br/>
		<tr><td><spring:message code="rheashradapter.no"/></td><td><spring:message code="rheashradapter.encounterId"/></td></tr>
			<c:forEach var="matchingEncounter" items="${getEncounterLog.matchingEncounters}" varStatus="varStatus">
		<tr <c:if test="${varStatus.index % 2 == 0}">class='evenRow'</c:if>>
		<td valign="top">${matchingEncounter.matchingEncountersId}</td>
		<td valign="top">${matchingEncounter.encounterId}</td>
	</tr>
	</c:forEach>
		</td>
	</tr>
	</c:if>
	
</table>
</div>


<%@ include file="/WEB-INF/template/footer.jsp" %>