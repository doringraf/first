DEFAULT_IFS=$IFS

JOB=$CI_JOB_ID
PROJECT=$CI_PROJECT_ID
CONTENTID='2127167811'
CONFLUENCE_AUTH_TOKEN=$CONFLUENCE_API_TOKEN
GITLAB_TOKEN=$GITLAB_API_TOKEN

TEMPLATE='{"version":{"number":$version},"title": $pagetitle,"type":"page","body":{"storage":{"value":$data,"representation":"storage","embeddedContent":[],"_expandable": $contentlink}}}'
PAGETITLE='Test Results'
CONTENTLINK=$( jq -n --arg contentlink "/rest/api/content/${CONTENTID}" '{"content": $contentlink}' )

# Get gitlab tests results output
GITLAB_OUTPUT=$(curl -H "PRIVATE-TOKEN:${GITLAB_TOKEN}" -H "Content-Type: text/html; charset=UTF-8" "https://gitlab.audacy.com/api/v4/projects/${PROJECT}/jobs/${JOB}/trace" | grep -B 2 -A 4 '├' )
IFS=$'\n' GITLAB_OUTPUT=(${GITLAB_OUTPUT})
IFS=$DEFAULT_IFS

# Get current content version
VERSION=$(curl -X GET "https://entercomdigitalservices.atlassian.net/wiki/rest/api/content/$CONTENTID" \
-H "Authorization: Basic ${CONFLUENCE_AUTH_TOKEN}" \
-H 'Content-Type: application/json' \
--data "$body" | jq .version | jq .number )

VERSION=$(( VERSION + 1))
echo "Updating confluence with version $VERSION"


sendToConfluence() {
    body=$( jq -n --arg version "${VERSION}" \
                --arg data "${1}" \
                --arg contentlink "${CONTENTLINK}" \
                --arg pagetitle "${PAGETITLE}" \
                    '{"version":{"number":$version},"title": $pagetitle,"type":"page","body":{"storage":{"value": $data,"representation":"storage","embeddedContent":[],"_expandable": $contentlink}}}' )

    curl -X PUT "https://entercomdigitalservices.atlassian.net/wiki/rest/api/content/$CONTENTID" \
                    -H "Authorization: Basic ${CONFLUENCE_AUTH_TOKEN}" \
                    -H 'Content-Type: application/json' \
                    --data "$body"
}


sendTestsResultToConfluence() {
    local output=''
    for (( j=0; j<${#GITLAB_OUTPUT[@]};j++ ))
    do
        if [[ ${GITLAB_OUTPUT[$j]} =~ '.' ]] || [[ ${GITLAB_OUTPUT[$j]} =~ '-' ]] || [[ ${GITLAB_OUTPUT[$j]} =~ 'passed' ]]
        then
            output="${output}<tr>"
            dataParts=(${GITLAB_OUTPUT[$j]})
            if [[ ${GITLAB_OUTPUT[$j]} =~ "passed" ]]
            then # success totals line
                output="${output}<tr><td>${dataParts[2]}</td><td>${dataParts[3]} ${dataParts[4]} ${dataParts[5]}</td><td>${dataParts[6]}</td><td>${dataParts[7]}</td><td>${dataParts[8]}</td><td>${dataParts[9]}</td><td>${dataParts[10]}</td><td>${dataParts[11]}</td></tr>"
            elif [[ "${dataParts[6]}" =~ "failed" ]]
            then # failed totals line
                output="${output}<tr><td>${dataParts[2]}</td><td>${dataParts[3]} ${dataParts[4]} ${dataParts[5]} ${dataParts[6]} ${dataParts[7]}</td><td>${dataParts[8]}</td><td>${dataParts[9]}</td><td>${dataParts[10]}</td><td>${dataParts[11]}</td><td>${dataParts[12]}</td><td>${dataParts[13]}</td></tr>"
            elif [[ ! "${dataParts[3]}" =~ "│" ]]
            then # indiviual spec totals line
                output="${output}<tr><td>${dataParts[2]}</td><td>${dataParts[3]}</td><td>${dataParts[4]}</td><td>${dataParts[5]}</td><td>${dataParts[6]}</td><td>${dataParts[7]}</td><td>${dataParts[8]}</td><td>${dataParts[9]}</td></tr>"
            else # name roll over spec line
                output="${output}<tr><td></td><td>${dataParts[2]}</td><td></td><td>${dataParts[5]}</td><td>${dataParts[6]}</td><td>${dataParts[7]}</td><td>${dataParts[8]}</td></tr>"
            fi
            
            output="${output}</tr>"
        
        fi
    done

    output=$( echo "<div style=\"width: 960px; left: -100px;\"><table><tr><td></td><td>Spec</td><td></td><td>Tests</td><td>Passing</td><td>Failing</td><td>Pending</td><td>Skipped</td></tr>${output}</table></div>" | sed -e 's/|/ /g' | sed -e 's/\x1b\[.\{1,5\}m//g' )
    sendToConfluence "${output}"
}

sendTestsResultToConfluence "${output}"
