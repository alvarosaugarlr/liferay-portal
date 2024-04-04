/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

// @ts-ignore

import {expect, mergeTests} from '@playwright/test';

import {loginTest} from '../../fixtures/loginTest';

export const test = mergeTests(
	loginTest()
);


test('Forgot Pasword Utility Page created and set as default', async ({
						   page,
					   }) => {
	await page.goto('/');
	await page.getByLabel('Open Product Menu').click();
	await page.getByRole('menuitem', { name: 'Site Builder' }).click();
	await page.getByRole('menuitem', { name: 'Pages' }).click();
	await page.locator('li').filter({ hasText: 'Utility Pages' }).click();
	await page.getByRole('button', { name: 'New' }).click();
	await page.getByRole('menuitem', { name: 'Forgot Password' }).click();
	await page.getByRole('button', { name: 'Blank' }).click();
	await page.getByPlaceholder('Name').click();
	await page.getByPlaceholder('Name').fill('fp-test');
	await page.getByPlaceholder('Name').press('Enter');
	await page.getByText('Button').click();
	await page.getByText('Go Somewhere').dblclick();
	await page.getByText('Go Somewhere').click();
	await page.locator('#fragment-lfdm-link').dblclick();
	await page.getByText('UP FP Hidden').click();
	await page.getByLabel('Fragments and Widgets').click();
	await page.getByLabel('Search Fragments and Widgets').click();
	await page.getByLabel('Search Fragments and Widgets').fill('for');
	await page.getByRole('menuitem', { name: 'Forgot Password Add Forgot Password Mark Forgot Password as Favorite' }).click();
	await page.getByLabel('Publish', { exact: true }).click();
	await page.locator('[id="_com_liferay_layout_admin_web_portlet_GroupPagesPortlet_entries_5"]').getByLabel('More actions').click();
	await page.getByRole('menuitem', { name: 'Mark as Default' }).click();
	await page.getByLabel('Test Test User Profile').click();
	await page.getByRole('menuitem', { name: 'Sign Out' }).click();
	await page.getByRole('button', { name: 'Sign In' }).click();
	await page.getByRole('link', { name: 'Forgot Password' }).click();
	await page.getByRole('link', { name: 'UP FP' }).click();

	await expect(page.getByRole('link', { name: 'UP FP' }))
		.toBeVisible({
			timeout: 8 * 1000,
		});
})

