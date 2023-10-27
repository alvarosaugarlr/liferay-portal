<%--
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
	String paramApplicationName = (String)request.getAttribute(SCIMConstants.PARAM_APPLICATION_NAME);
	String paramMatcherField = (String)request.getAttribute(SCIMConstants.PARAM_MATCHER_FIELD);
	String paramToken = (String)request.getAttribute(SCIMConstants.PARAM_TOKEN);

%>

	<aui:input name="<%= Constants.CMD %>" type="hidden" value="" />


<aui:input label="application-name" name="applicationName" required="<%= true %>" type="text" value="<%= paramApplicationName %>" />


<aui:select label="scim_matcherField" name="matcherField" required="<%= true %>" value="<%= paramMatcherField %>">
	<aui:option label="" value="" />

	<%
		for (String matcherField : SCIMConstants.MATCHER_FIELD) {
	%>

			<aui:option label="<%= matcherField %>" value="<%= matcherField %>" />

	<%
		}
	%>

</aui:select>

<c:choose>
	<c:when test='<%= paramApplicationName != null %>'>
		<aui:input label="access-token" name="accessToken" type="textarea" value="<%= paramToken %>" />


		<aui:button
			name="genetareAccessToken"
			id="genetareAccessToken"
			label="discard-changes"
			small="<%= true %>"
			value="generate"
		/>
		<c:choose>
			<c:when test='<%= paramToken != null %>'>
				<aui:button
					name="revokeAccessToken"
					id="revokeAccessToken"
					label="discard-changes"
					small="<%= true %>"
					value="revoke"
				/>
			</c:when>
		</c:choose>
	</c:when>
</c:choose>

<script>
	var genetareAccessToken = document.getElementById(
		'<portlet:namespace />genetareAccessToken'
	);

	if (genetareAccessToken) {
		genetareAccessToken.addEventListener('click', (event) => {
			Liferay.Util.openConfirmModal({
				message:
					'<liferay-ui:message key="are-you-sure-you-want-to-generate-access-token" />',
				onConfirm: (isConfirmed) => {
					if (isConfirmed) {
						var form = window.document['<portlet:namespace />fm'];
						form['<portlet:namespace /><%= Constants.CMD %>'].value = '<%= SCIMWebKeys.SCIM_GENERATE %>';

						form.submit();



					}
				},
			});
		});
	}



	var revokeAccessToken = document.getElementById(
		'<portlet:namespace />revokeAccessToken'
	);

	if (revokeAccessToken) {
		revokeAccessToken.addEventListener('click', (event) => {
			Liferay.Util.openConfirmModal({
				message:
					'<liferay-ui:message key="are-you-sure-you-want-to-revoke-access-tokens" />',
				onConfirm: (isConfirmed) => {
					if (isConfirmed) {
						var form = window.document['<portlet:namespace />fm'];
						form['<portlet:namespace /><%= Constants.CMD %>'].value = '<%= SCIMWebKeys.SCIM_REVOKE %>';

						form.submit();



					}
				},
			});
		});
	}
</script>
