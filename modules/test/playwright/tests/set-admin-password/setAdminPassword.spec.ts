/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page, expect, mergeTests} from '@playwright/test';

import {liferayConfig} from '../../liferay.config';
//import {
  //  VirtualInstancesPage
//} from "../../pages/portal-instances-web/VirtualInstancesPage";
import {userData} from "../../utils/performLogin";
import {loginTest} from "../../fixtures/loginTest";
import {
    applicationsMenuPageTest
} from "../../fixtures/applicationsMenuPageTest";
import {
    serverAdministrationPageTest
} from "../../fixtures/serverAdministrationPageTest";
import {userGroupsPageTest} from "../../fixtures/userGroupsPageTest";
import {
    usersAndOrganizationsPagesTest
} from "../../fixtures/usersAndOrganizationsPagesTest";
import {virtualInstancesPagesTest} from '../../fixtures/virtualInstancesPagesTest';
import getRandomString from "../../utils/getRandomString";



export const test = mergeTests(
    applicationsMenuPageTest,
    serverAdministrationPageTest,
    userGroupsPageTest,
    usersAndOrganizationsPagesTest,
    virtualInstancesPagesTest
);

test('LPD-test', async ({
                            page,
                        }) => {
    await page.goto(liferayConfig.environment.baseUrl);
    await expect(page.getByRole('heading', { name: 'Set Password' }))
        .toBeVisible({
            timeout: 10 * 1000,
        });

});

test(
    'virutal default.admin.password blank',
    async ({
               page,
               virtualInstancesPage
    }) => {
    await page.goto(liferayConfig.environment.baseUrl);
    await expect(page.getByRole('heading', { name: 'Set Password' }))
        .toBeVisible({
            timeout: 10 * 1000,
        });


    const {name, password, surname} = userData['test'];

    await page.getByLabel('Password', { exact: true }).fill(password);

    await page.getByLabel('Reenter Password').fill(password);

    const signInButton = page.getByRole('button', {name: 'Save'});
    await signInButton.click();

    await expect(page.getByLabel(`${name} ${surname}`)).toBeVisible({
        timeout: 30 * 1000,
    });



//const termsOfUseButton = page.getByRole('button', {name: 'Done'});
  //  await termsOfUseButton.click();
    const nameInstance = getRandomString();
    //const screenName = getRandomString();
    //const emailAddress = getRandomString();
    //const pàssword = getRandomString();

        //const virtualInstancesPage = new VirtualInstancesPage(page);
    await virtualInstancesPage.addNewVirtualInstanceSettingAdminUser(nameInstance, name, name+'@liferay.com', password);

 /*   const virtualInstancesPage = new VirtualInstancesPage(page);

    virtualInstancesPage.goto();

    await page.getByRole('button', {name: 'Add'}).click();

    //await virtualInstancesPage.addNewVirtualInstance('asdf');

    await page.getByRole('button', {name: 'Add'}).click();

    expect(page.getByText('The Password field is required.')).toBeVisible();

  */

});