'use strict';

/*

 Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 This software is published under the GPL GNU General Public License.
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

 This software was written for the
 Department of Family Medicine
 McMaster University
 Hamilton
 Ontario, Canada

 */
angular.module('Patient.Search').controller('Patient.Search.PatientSearchController', [
	'$uibModal',
	'$http',
	'$scope',
	'$timeout',
	'$resource',
	'$state',
	'$stateParams',
	'$location',
	'NgTableParams',
	'securityService',
	'demographicService',

	function(
		$uibModal,
		$http,
		$scope,
		$timeout,
		$resource,
		$state,
		$stateParams,
		$location,
		NgTableParams,
		securityService,
		demographicService)
	{

		var quickSearchTerm = ($location.search()).term;

		//type and term for now
		$scope.search = {
			type: 'Name',
			term: "",
			active: true,
			integrator: false,
			outofdomain: true
		};

		$scope.lastResponse = '';

		securityService.hasRights(
		{
			items: [
			{
				objectName: '_demographic',
				privilege: 'w'
			},
			{
				objectName: '_demographic',
				privilege: 'r'
			}]
		}).then(function(result)
		{
			if (result.content != null && result.content.length == 2)
			{
				$scope.demographicWriteAccess = result.content[0];
				$scope.demographicReadAccess = result.content[1];

				if ($scope.demographicReadAccess)
				{
					$scope.tableParams = new NgTableParams(
					{
						page: 1, // show first page
						count: 10,
						sorting:
						{
							Name: 'asc' // initial sorting
						}
					},
					{
						getData: function(params)
						{
							$scope.integratorResults = null;
							var count = params.url().count;
							var page = params.url().page;

							$scope.search.params = params.url();

							demographicService.search($scope.search, ((page - 1) * count), count).then(function(result)
							{
								params.total(result.total);
								// $defer.resolve(result.content);
								$scope.lastResponse = result.content;
							}, function(reason)
							{
								alert("demo-service:" + reason);
							});

							if ($scope.search.integrator == true)
							{
								//Note - I put in this arbitrary maximum
								demographicService.searchIntegrator($scope.search, 100).then(function(result)
								{
									$scope.integratorResults = result;
								}, function(reason)
								{
									alert("remote-demo-service:" + reason);
								});
							}
						}
					});
					if (quickSearchTerm != null)
					{
						$scope.search.term = quickSearchTerm;
						$scope.tableParams.reload();
					}
				}
			}
			else
			{
				alert('failed to load rights');
			}
		}, function(reason)
		{
			alert(reason);
		});




		$scope.doSearch = function()
		{
			if ($scope.search.type == "DOB")
			{
				if ($scope.search.term.match("^[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}$") || $scope.search.term.match("^[0-9]{4}/[0-9]{1,2}/[0-9]{1,2}$"))
				{
					$scope.search.term = $scope.search.term.replace(/\//g, "-");
					var datePart = $scope.search.term.split("-");
					$scope.search.term = datePart[0] + "-" + pad0(datePart[1]) + "-" + pad0(datePart[2]);
				}
				else
				{
					alert("Please enter Date of Birth in format YYYY-MM-DD.");
					return;
				}
			}
			$scope.tableParams.reload();
		};

		$scope.doClear = function()
		{
			$scope.search = {
				type: 'Name',
				term: "",
				active: true,
				integrator: false,
				outofdomain: true
			};
			$scope.tableParams.reload();
		};

		$scope.clearButMaintainSearchType = function()
		{
			$scope.search = {
				type: $scope.search.type,
				term: "",
				active: true,
				integrator: false,
				outofdomain: true
			};
			$scope.tableParams.reload();

			if ($scope.search.type == "DOB") $scope.searchTermPlaceHolder = "YYYY-MM-DD";
			else $scope.searchTermPlaceHolder = "Search Term";
		};

		$scope.searchTermPlaceHolder = "Search Term";

		$scope.loadRecord = function(demographicNo)
		{
			$state.go('record.details',
			{
				demographicNo: demographicNo,
				hideNote: true
			});
		};

		$scope.showIntegratorResults = function()
		{
			var result = ($scope.integratorResults != null && $scope.integratorResults.total > 0) ? $scope.integratorResults.content : [];
			var total = ($scope.integratorResults != null) ? $scope.integratorResults.total : 0;
			var modalInstance = $uibModal.open(
			{
				templateUrl: 'patientsearch/remotePatientResults.jsp',
				controller: 'RemotePatientResultsController',
				resolve:
				{
					results: function()
					{
						return result;
					},
					total: function()
					{
						return total;
					}
				}
			});
		};
	}
]);

function pad0(n)
{
	if (n.length > 1) return n;
	else return "0" + n;
}