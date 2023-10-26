<%--
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
	SCIMConfiguration scimConfiguration = (SCIMConfiguration)request.getAttribute(SCIMConfiguration.class.getName());
%>

	<aui:input name="<%= Constants.CMD %>" type="hidden" value="" />


<aui:input label="applicationName" name="applicationName" type="text" value="<%= scimConfiguration.applicationName() %>" />


<aui:select label="matcherField" name="matcherField" required="<%= true %>" value="<%= scimConfiguration.matcherField() %>">
	<aui:option label="" value="" />

	<%
		for (String matcherField : SCIMConstants.MATCHER_FIELD) {
	%>

	<aui:option label="<%= matcherField %>" value="<%= matcherField %>" />

	<%
		}
	%>

</aui:select>


<aui:input label="accessToken" name="accessToken" type="text" value="<%= scimConfiguration.applicationName() %>" />


<aui:button
	name="genetareAccessToken"
	id="genetareAccessToken"
	label="discard-changes"
	small="<%= true %>"
	value="generate"
/>

<aui:button
	name="revokeAccessToken"
	id="revokeAccessToken"
	label="discard-changes"
	small="<%= true %>"
	value="revoke"
/>

<script>
	var deleteButtonElement = document.getElementById(
		'<portlet:namespace />genetareAccessToken'
	);

	if (deleteButtonElement) {
		deleteButtonElement.addEventListener('click', (event) => {
			Liferay.Util.openConfirmModal({
				message:
					'<liferay-ui:message key="are-you-sure-you-want-to-delete-this" />',
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



	var deleteButtonElement = document.getElementById(
		'<portlet:namespace />revokeAccessToken'
	);

	if (deleteButtonElement) {
		deleteButtonElement.addEventListener('click', (event) => {
			Liferay.Util.openConfirmModal({
				message:
					'<liferay-ui:message key="are-you-sure-you-want-to-delete-this" />',
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
